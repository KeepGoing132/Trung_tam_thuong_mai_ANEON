package com.company.aneon.f10;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import com.company.aneon.f10.service.QrCodeServiceImpl;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.error.ErrorCode;
import com.company.aneon.shared.security.AccessScope;
import com.company.aneon.shared.security.AccessScopePort;
import com.company.aneon.shared.security.LocalIdentityFilter;

import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QrCodeServiceImplTest {

    private SuKienRepository suKienRepository;
    private MaQrThamGiaRepository qrRepository;
    private LuotThamGiaSuKienRepository attendanceRepository;
    private PersonLookupPort personLookupPort;
    private QrCodeServiceImpl service;

    private final UUID actorId = UUID.randomUUID();
    private final UUID eventId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final Instant start = Instant.parse("2026-11-10T09:00:00Z");
    private final Instant end = Instant.parse("2026-11-12T22:00:00Z");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-11T00:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        suKienRepository = mock(SuKienRepository.class);
        qrRepository = mock(MaQrThamGiaRepository.class);
        attendanceRepository = mock(LuotThamGiaSuKienRepository.class);
        CustomerLookupPort customerLookupPort = mock(CustomerLookupPort.class);
        when(customerLookupPort.customerExists(any())).thenReturn(true);
        personLookupPort = mock(PersonLookupPort.class);
        AccessScopePort accessScopePort = mock(AccessScopePort.class);
        when(accessScopePort.currentScope()).thenReturn(AccessScope.ALL);
        when(accessScopePort.currentActorId()).thenReturn(Optional.of(actorId));

        service = new QrCodeServiceImpl(suKienRepository, qrRepository, attendanceRepository,
                customerLookupPort, personLookupPort, accessScopePort, CLOCK,
                "https://aneon.local/checkin");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void login(UUID id, String... roles) {
        LocalIdentityFilter.LocalAuthenticationToken token = new LocalIdentityFilter.LocalAuthenticationToken(
                new com.company.aneon.shared.security.AneonPrincipal(id, "u-" + id,
                        java.util.Set.of(roles)),
                com.company.aneon.shared.security.RolePermissions.authoritiesForRoles(java.util.Set.of(roles)));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private SuKien event(EventStatus status) {
        SuKien event = new SuKien();
        event.setId(eventId);
        event.setCode("EVT-001");
        event.setName("Su kien");
        event.setStatus(status);
        event.setStartAt(start);
        event.setEndAt(end);
        event.setProposedBy(actorId);
        org.springframework.test.util.ReflectionTestUtils.setField(event, "version", 1L);
        when(suKienRepository.findWithLockById(eventId)).thenReturn(Optional.of(event));
        when(suKienRepository.findById(eventId)).thenReturn(Optional.of(event));
        return event;
    }

    @Test
    void khongSinhQrChoSuKienChoDuyet() {
        event(EventStatus.PENDING_APPROVAL);
        login(actorId, "MARKETING");
        assertThatThrownBy(() -> service.generate(eventId))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.BUSINESS_CONFLICT);
    }

    @Test
    void sinhQrSetExpiresBangEndAtVaChiLuuHash() {
        SuKien event = event(EventStatus.APPROVED);
        login(actorId, "MARKETING");
        EventDtos.QrCodeDto dto = service.generate(eventId);
        assertThat(dto.active()).isTrue();
        assertThat(dto.expiresAt()).isEqualTo(end);
        assertThat(dto.checkInUrl()).contains("token=");
        org.mockito.ArgumentCaptor<MaQrThamGia> captor =
                org.mockito.ArgumentCaptor.forClass(MaQrThamGia.class);
        org.mockito.Mockito.verify(qrRepository).save(captor.capture());
        MaQrThamGia saved = captor.getValue();
        assertThat(saved.getTokenHash()).hasSize(64).doesNotContain("token");
        assertThat(saved.getExpiresAt()).isEqualTo(event.getEndAt());
    }

    @Test
    void sinhQrThuHoiQrCu() {
        event(EventStatus.APPROVED);
        login(actorId, "MARKETING");
        MaQrThamGia old = new MaQrThamGia();
        old.setId(UUID.randomUUID());
        old.setActive(true);
        when(qrRepository.findActiveWithLockByEventId(eventId)).thenReturn(java.util.List.of(old));
        service.generate(eventId);
        assertThat(old.isActive()).isFalse();
    }

    @Test
    void checkInNgoaiThoiGianSuKienNem409() {
        SuKien event = event(EventStatus.ONGOING);
        // Dua su kien ra khoi cua so thoi gian so voi clock dinh
        event.setStartAt(Instant.parse("2026-11-01T09:00:00Z"));
        event.setEndAt(Instant.parse("2026-11-02T22:00:00Z"));
        login(actorId, "MARKETING", "OPERATIONS");
        EventDtos.CheckInRequest request = new EventDtos.CheckInRequest(
                "token", customerId, "q1");
        assertThatThrownBy(() -> service.checkIn(eventId, request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("thoi gian su kien");
    }

    @Test
    void checkInTokenSaiNem409() {
        event(EventStatus.ONGOING);
        login(actorId, "OPERATIONS");
        when(qrRepository.findByTokenHashAndActiveTrue(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.checkIn(eventId,
                new EventDtos.CheckInRequest("sai", customerId, null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("khong hop le");
    }

    @Test
    void checkInLapTraKetQuaLanDauKhongThuongLap() {
        SuKien event = event(EventStatus.ONGOING);
        login(actorId, "OPERATIONS");
        String token = "tok";
        MaQrThamGia qr = new MaQrThamGia();
        qr.setId(UUID.randomUUID());
        qr.setActive(true);
        qr.setExpiresAt(end);
        qr.setTokenHash(QrCodeServiceImpl.sha256Hex(token));
        qr.setEvent(event);
        when(qrRepository.findByTokenHashAndActiveTrue(QrCodeServiceImpl.sha256Hex(token)))
                .thenReturn(Optional.of(qr));
        LuotThamGiaSuKien existing = new LuotThamGiaSuKien();
        existing.setId(UUID.randomUUID());
        existing.setEventId(eventId);
        existing.setCustomerId(customerId);
        existing.setCheckInAt(start);
        when(attendanceRepository.findByEventIdAndCustomerId(eventId, customerId))
                .thenReturn(Optional.of(existing));

        EventDtos.CheckInResult result = service.checkIn(eventId,
                new EventDtos.CheckInRequest(token, customerId, null));
        assertThat(result.duplicate()).isTrue();
        assertThat(result.checkInId()).isEqualTo(existing.getId());
    }

    @Test
    void khachTuCheckInDungLienKetCuaMinh() {
        SuKien event = event(EventStatus.ONGOING);
        login(actorId, "CUSTOMER");
        when(personLookupPort.customerLinkOf(actorId)).thenReturn(Optional.of(customerId));
        String token = "tok2";
        MaQrThamGia qr = new MaQrThamGia();
        qr.setId(UUID.randomUUID());
        qr.setActive(true);
        qr.setExpiresAt(end);
        qr.setTokenHash(QrCodeServiceImpl.sha256Hex(token));
        qr.setEvent(event);
        when(qrRepository.findByTokenHashAndActiveTrue(QrCodeServiceImpl.sha256Hex(token)))
                .thenReturn(Optional.of(qr));
        EventDtos.CheckInResult result = service.checkIn(eventId,
                new EventDtos.CheckInRequest(token, null, null));
        assertThat(result.customerId()).isEqualTo(customerId);
    }

    @Test
    void khachKhongDuocCheckInChoNguoiKhac() {
        event(EventStatus.ONGOING);
        login(actorId, "CUSTOMER");
        when(personLookupPort.customerLinkOf(actorId)).thenReturn(Optional.of(UUID.randomUUID()));
        assertThatThrownBy(() -> service.checkIn(eventId,
                new EventDtos.CheckInRequest("tok", customerId, null)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }
}
