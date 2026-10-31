package com.company.aneon.f10.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import com.company.aneon.f10.constant.EventStatus;
import com.company.aneon.f10.dto.EventDtos;
import com.company.aneon.f10.entity.LuotThamGiaSuKien;
import com.company.aneon.f10.entity.MaQrThamGia;
import com.company.aneon.f10.entity.SuKien;
import com.company.aneon.f10.integration.CustomerLookupPort;
import com.company.aneon.f10.integration.PersonLookupPort;
import com.company.aneon.f10.repository.LuotThamGiaSuKienRepository;
import com.company.aneon.f10.repository.MaQrThamGiaRepository;
import com.company.aneon.f10.repository.SuKienRepository;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.error.ErrorCode;
import com.company.aneon.shared.security.AccessScopePort;
import com.company.aneon.shared.security.RolePermissions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * F10UC01.3 + check-in bo tro. BR_SUTI_04: mot QR hieu luc moi su kien
 * (unique V2), check-in chi trong thoi gian su kien; token chi luu hash
 * (D0007).
 */
@Service
public class QrCodeServiceImpl implements QrCodeService {

    private final SuKienRepository suKienRepository;
    private final MaQrThamGiaRepository qrRepository;
    private final LuotThamGiaSuKienRepository attendanceRepository;
    private final CustomerLookupPort customerLookupPort;
    private final PersonLookupPort personLookupPort;
    private final AccessScopePort accessScopePort;
    private final Clock clock;
    private final String publicCheckInBaseUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public QrCodeServiceImpl(SuKienRepository suKienRepository, MaQrThamGiaRepository qrRepository,
                             LuotThamGiaSuKienRepository attendanceRepository,
                             CustomerLookupPort customerLookupPort, PersonLookupPort personLookupPort,
                             AccessScopePort accessScopePort, Clock clock,
                             @Value("${aneon.public-checkin-base-url:}") String publicCheckInBaseUrl) {
        this.suKienRepository = suKienRepository;
        this.qrRepository = qrRepository;
        this.attendanceRepository = attendanceRepository;
        this.customerLookupPort = customerLookupPort;
        this.personLookupPort = personLookupPort;
        this.accessScopePort = accessScopePort;
        this.clock = clock;
        this.publicCheckInBaseUrl = publicCheckInBaseUrl;
    }

    @Override
    @Transactional
    public EventDtos.QrCodeDto generate(UUID eventId) {
        if (publicCheckInBaseUrl == null || publicCheckInBaseUrl.isBlank()) {
            throw ApiException.dependencyUnavailable(
                    "Chua cau hinh aneon.public-checkin-base-url cho URL check-in cong khai");
        }
        SuKien event = suKienRepository.findWithLockById(eventId)
                .orElseThrow(() -> ApiException.notFound("Su kien", eventId));
        if (event.getStatus() != EventStatus.APPROVED && event.getStatus() != EventStatus.ONGOING) {
            throw ApiException.conflict("Chi sinh QR cho su kien da duyet va con duoc check-in");
        }
        requirePermission(RolePermissions.F10_EVENT_QR);

        // Thu hoi QR cu trong cung transaction; unique(event_id, active_guard)
        // chan hai request tao hai QR active (D0007). Request thua trong canh
        // tranh nhan 409 (retryable) thay vi hai QR active.
        qrRepository.findActiveWithLockByEventId(eventId).forEach(qr -> qr.setActive(false));

        String token = newToken();
        MaQrThamGia qr = new MaQrThamGia();
        qr.setId(UUID.randomUUID());
        qr.setEvent(event);
        qr.setTokenHash(sha256Hex(token));
        // URL chua token goc - chi tra cho actor co quyen, khong log (rule 05)
        qr.setCheckInUrl(publicCheckInBaseUrl + (publicCheckInBaseUrl.contains("?") ? "&" : "?")
                + "token=" + token + "&event=" + eventId);
        // expiresAt khong vuot qua endAt su kien (khong noi cua so check-in)
        qr.setExpiresAt(event.getEndAt());
        qr.setActive(true);
        qr.setCreatedBy(requireActor());
        qrRepository.save(qr);
        return toDto(qr);
    }

    @Override
    @Transactional
    public EventDtos.CheckInResult checkIn(UUID eventId, EventDtos.CheckInRequest request) {
        SuKien event = suKienRepository.findById(eventId)
                .filter(e -> e.getArchivedAt() == null)
                .orElseThrow(() -> ApiException.notFound("Su kien", eventId));
        if (request.qrToken() == null || request.qrToken().isBlank()) {
            throw ApiException.validation("qrToken", "Token QR khong duoc trong");
        }
        UUID actorId = requireActor();
        UUID customerId = resolveCustomerId(request.customerId(), actorId);
        if (!customerLookupPort.customerExists(customerId)) {
            throw ApiException.notFound("Khach mua sam", customerId);
        }

        Instant now = Instant.now(clock);
        if (event.getStatus() != EventStatus.APPROVED && event.getStatus() != EventStatus.ONGOING) {
            throw ApiException.conflict("Su kien khong dang nhan check-in (trang thai " + event.getStatus() + ")");
        }
        if (event.getStartAt() != null && now.isBefore(event.getStartAt())
                || event.getEndAt() != null && now.isAfter(event.getEndAt())) {
            throw ApiException.conflict("Check-in chi trong thoi gian su kien");
        }

        MaQrThamGia qr = qrRepository.findByTokenHashAndActiveTrue(sha256Hex(request.qrToken().trim()))
                .orElseThrow(() -> new ApiException(ErrorCode.BUSINESS_CONFLICT,
                        "QR khong hop le hoac da bi thu hoi"));
        if (!eventId.equals(qr.getEvent().getId())) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "QR khong thuoc su kien nay");
        }
        if (qr.getExpiresAt() != null && now.isAfter(qr.getExpiresAt())) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "QR da het han");
        }

        // Replay (quet lai): tra lai luot da co, khong ghi them khong thuong lap.
        // Canh tranh dong thoi bi UNIQUE chuyen thanh 409 qua GlobalExceptionHandler.
        var existingEntry = attendanceRepository.findByEventIdAndCustomerId(eventId, customerId);
        if (existingEntry.isPresent()) {
            LuotThamGiaSuKien existing = existingEntry.get();
            return new EventDtos.CheckInResult(existing.getId(), eventId, customerId,
                    existing.getCheckInAt(), true);
        }

        LuotThamGiaSuKien entry = new LuotThamGiaSuKien();
        entry.setId(UUID.randomUUID());
        entry.setEventId(eventId);
        entry.setCustomerId(customerId);
        entry.setQrCodeId(qr.getId());
        entry.setCheckInAt(now);
        entry.setScanningDevice(request.scanningDevice());
        entry.setCreatedBy(actorId);
        entry.setCreatedAt(now);
        attendanceRepository.save(entry);
        return new EventDtos.CheckInResult(entry.getId(), eventId, customerId, entry.getCheckInAt(), false);
    }

    /** Khach tu check-in cho minh; nhan vien ho tro can quyen assist + customerId (D0007). */
    private UUID resolveCustomerId(UUID requestedCustomerId, UUID actorId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isCustomer = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CUSTOMER".equals(a.getAuthority()));
        if (isCustomer && requestedCustomerId == null) {
            UUID linked = personLookupPort.customerLinkOf(actorId)
                    .orElseThrow(() -> ApiException.forbidden(
                            "Tai khoan khach hang chua lien ket khach mua sam"));
            return linked;
        }
        boolean isAssistant = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> new SimpleGrantedAuthority(RolePermissions.F10_CHECKIN_ASSIST)
                        .equals(a));
        if (isAssistant) {
            if (requestedCustomerId == null) {
                throw ApiException.validation("customerId", "Nhan vien ho tro phai chi dinh khach hang");
            }
            return requestedCustomerId;
        }
        if (requestedCustomerId == null) {
            throw ApiException.forbidden("Khong co quyen check-in cho khach khac");
        }
        if (personLookupPort.customerLinkOf(actorId)
                .map(linked -> linked.equals(requestedCustomerId)).orElse(false)) {
            return requestedCustomerId;
        }
        throw ApiException.forbidden("Khong co quyen check-in cho khach khac");
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 khong kha dung", ex);
        }
    }

    private void requirePermission(String permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().stream()
                .noneMatch(a -> new SimpleGrantedAuthority(permission).equals(a))) {
            throw ApiException.forbidden("Khong co quyen thuc hien thao tac nay");
        }
    }

    private UUID requireActor() {
        return accessScopePort.currentActorId().orElseThrow(
                () -> new ApiException(ErrorCode.UNAUTHENTICATED, "Khong xac dinh duoc nguoi thao tac"));
    }

    private EventDtos.QrCodeDto toDto(MaQrThamGia qr) {
        return new EventDtos.QrCodeDto(qr.getId(), qr.getCheckInUrl(), qr.getImageStorageKey(),
                qr.getExpiresAt(), qr.isActive());
    }
}
