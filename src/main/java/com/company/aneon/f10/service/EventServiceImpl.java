package com.company.aneon.f10.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.company.aneon.f01.entity.ViTri;
import com.company.aneon.f10.constant.EventStatus;
import com.company.aneon.f10.dto.EventDtos;
import com.company.aneon.f10.entity.ChiPhiThucTe;
import com.company.aneon.f10.entity.HoatDongSuKien;
import com.company.aneon.f10.entity.SuKien;
import com.company.aneon.f10.entity.SuKienCuaHang;
import com.company.aneon.f10.integration.F01LookupPort;
import com.company.aneon.f10.integration.FileAttachmentPort;
import com.company.aneon.f10.integration.PersonLookupPort;
import com.company.aneon.f10.repository.ChiPhiThucTeRepository;
import com.company.aneon.f10.repository.HoatDongSuKienRepository;
import com.company.aneon.f10.repository.SuKienCuaHangRepository;
import com.company.aneon.f10.repository.SuKienRepository;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.error.ErrorCode;
import com.company.aneon.shared.reference.TepDinhKemLookupRepository;
import com.company.aneon.shared.security.AccessScopePort;
import com.company.aneon.shared.security.RolePermissions;
import com.company.aneon.shared.web.PageResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * F10UC01.1, 01.2, 01.5 + chi tiet/chi phi bo tro. Trang thai de xuat:
 * tao la gui -> PENDING_APPROVAL (D0004); chi pending duoc duyet/tu choi.
 */
@Service
public class EventServiceImpl implements EventService {

    /** Trang thai su kien dang giu cho vi tri (rule f10: khong cho draft/huy giu cho). */
    public static final Set<EventStatus> OCCUPYING_STATUSES =
            Set.of(EventStatus.PENDING_APPROVAL, EventStatus.APPROVED, EventStatus.ONGOING);

    private final SuKienRepository suKienRepository;
    private final HoatDongSuKienRepository hoatDongRepository;
    private final ChiPhiThucTeRepository chiPhiRepository;
    private final SuKienCuaHangRepository suKienCuaHangRepository;
    private final F01LookupPort f01LookupPort;
    private final PersonLookupPort personLookupPort;
    private final FileAttachmentPort fileAttachmentPort;
    private final TepDinhKemLookupRepository tepDinhKemLookupRepository;
    private final AccessScopePort accessScopePort;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;
    private final long maxFileBytes;
    private final int maxFilesPerProposal;
    private final List<String> allowedExtensions;
    private final jakarta.persistence.EntityManager entityManager;

    public EventServiceImpl(SuKienRepository suKienRepository, HoatDongSuKienRepository hoatDongRepository,
                            ChiPhiThucTeRepository chiPhiRepository, SuKienCuaHangRepository suKienCuaHangRepository,
                            F01LookupPort f01LookupPort, PersonLookupPort personLookupPort,
                            FileAttachmentPort fileAttachmentPort,
                            TepDinhKemLookupRepository tepDinhKemLookupRepository,
                            AccessScopePort accessScopePort,
                            ApplicationEventPublisher eventPublisher, Clock clock,
                            @Value("${aneon.upload.max-file-bytes}") long maxFileBytes,
                            @Value("${aneon.upload.max-files-per-proposal}") int maxFilesPerProposal,
                            @Value("${aneon.upload.allowed-extensions}") List<String> allowedExtensions,
                            jakarta.persistence.EntityManager entityManager) {
        this.suKienRepository = suKienRepository;
        this.hoatDongRepository = hoatDongRepository;
        this.chiPhiRepository = chiPhiRepository;
        this.suKienCuaHangRepository = suKienCuaHangRepository;
        this.f01LookupPort = f01LookupPort;
        this.personLookupPort = personLookupPort;
        this.fileAttachmentPort = fileAttachmentPort;
        this.tepDinhKemLookupRepository = tepDinhKemLookupRepository;
        this.accessScopePort = accessScopePort;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.maxFileBytes = maxFileBytes;
        this.maxFilesPerProposal = maxFilesPerProposal;
        this.allowedExtensions = allowedExtensions;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public EventDtos.EventSummary createProposal(EventDtos.CreateEventProposalRequest request) {
        return createEvent(request, EventStatus.PENDING_APPROVAL);
    }

    @Override
    @Transactional
    public EventDtos.EventSummary createDraft(EventDtos.CreateEventProposalRequest request) {
        return createEvent(request, EventStatus.DRAFT);
    }

    private EventDtos.EventSummary createEvent(EventDtos.CreateEventProposalRequest request, EventStatus status) {
        String code = request.code().trim();
        if (suKienRepository.existsByCodeIgnoreCase(code)) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Ma su kien da ton tai: " + code);
        }
        UUID actorId = requireActor();
        SuKien event = new SuKien();
        event.setId(UUID.randomUUID());
        event.setCode(code);
        event.setStatus(status);
        event.setProposedBy(actorId);
        event.setCreatedBy(actorId);
        saveEventDetails(event, request, actorId);
        return toSummary(event);
    }

    private void saveEventDetails(SuKien event, EventDtos.CreateEventProposalRequest request, UUID actorId) {
        if (!request.startAt().isBefore(request.endAt())) {
            throw ApiException.validation("endAt", "Thoi gian ket thuc phai sau thoi gian bat dau");
        }

        F01LookupPort.LocationRef location = f01LookupPort.findLocation(request.locationId())
                .orElseThrow(() -> ApiException.notFound("Vi tri", request.locationId()));
        assertLocationUsable(location, request.locationId());

        List<UUID> storeIds = request.participatingStoreIds() == null ? List.of()
                : request.participatingStoreIds().stream().distinct().toList();
        validateStores(storeIds);

        validateProposalFiles(request.proposalFileKeys(), actorId);

        event.setName(request.name().trim());
        event.setType(request.type());
        event.setLocation(viTriReference(request.locationId()));
        event.setStartAt(request.startAt());
        event.setEndAt(request.endAt());
        event.setEstimatedBudget(request.estimatedBudget());
        event.setTargetAttendees(request.targetAttendees());
        event.setPublicVisible(Boolean.TRUE.equals(request.publicVisible()));
        suKienRepository.save(event);

        var existing = suKienCuaHangRepository.findByEventId(event.getId());
        var existingIds = existing.stream().map(SuKienCuaHang::getStoreId).collect(java.util.stream.Collectors.toSet());
        suKienCuaHangRepository.deleteAll(existing.stream().filter(link -> !storeIds.contains(link.getStoreId())).toList());
        for (UUID storeId : storeIds.stream().filter(id -> !existingIds.contains(id)).toList()) {
            suKienCuaHangRepository.save(new SuKienCuaHang(UUID.randomUUID(), event.getId(), storeId));
        }
    }

    @Override
    @Transactional
    public EventDtos.EventDetail updateDraft(UUID id, long version, EventDtos.CreateEventProposalRequest request) {
        UUID actorId = requireActor();
        SuKien event = requireOwnDraft(id, version, actorId);
        if (!event.getCode().equals(request.code().strip())) {
            throw ApiException.validation("code", "Ma su kien khong thay doi sau khi tao");
        }
        entityManager.lock(event, jakarta.persistence.LockModeType.PESSIMISTIC_FORCE_INCREMENT);
        saveEventDetails(event, request, actorId);
        suKienRepository.flush();
        return toDetail(event);
    }

    @Override
    @Transactional
    public EventDtos.EventDetail submitDraft(UUID id, long version) {
        SuKien event = requireOwnDraft(id, version, requireActor());
        event.setStatus(EventStatus.PENDING_APPROVAL);
        suKienRepository.flush();
        return toDetail(event);
    }

    private SuKien requireOwnDraft(UUID id, long version, UUID actorId) {
        SuKien event = suKienRepository.findWithLockById(id).orElseThrow(() -> ApiException.notFound("Su kien", id));
        EventAccess.requireInScope(event, accessScopePort);
        if (!actorId.equals(event.getProposedBy())) throw ApiException.forbidden("Chi nguoi lap duoc sua va gui ban nhap");
        if (event.getStatus() != EventStatus.DRAFT) throw ApiException.conflict("Su kien khong con la ban nhap");
        if (version != event.getVersion()) throw ApiException.conflict("Phien ban du lieu da thay doi");
        return event;
    }

    @Override
    @Transactional
    public EventDtos.EventDetail approve(UUID eventId, EventDtos.ApproveEventRequest request) {
        SuKien event = suKienRepository.findWithLockById(eventId)
                .orElseThrow(() -> ApiException.notFound("Su kien", eventId));
        EventAccess.requireInScope(event, accessScopePort);
        if (event.getStatus() != EventStatus.PENDING_APPROVAL) {
            throw ApiException.conflict("Chi de xuat o trang thai PENDING_APPROVAL duoc duyet/tu choi");
        }
        if (request.version() != event.getVersion()) {
            throw ApiException.conflict("Phien ban du lieu da thay doi (version=" + event.getVersion() + ")");
        }
        UUID actorId = requireActor();
        String action = request.action() == null ? "" : request.action().trim().toUpperCase(java.util.Locale.ROOT);
        switch (action) {
            case "APPROVE" -> {
                if (request.approvedBudget() == null
                        || request.approvedBudget().compareTo(BigDecimal.ZERO) <= 0) {
                    throw ApiException.validation("approvedBudget", "Duyet phai kem ngan sach duyet duong");
                }
                event.setStatus(EventStatus.APPROVED);
                event.setApprovedBudget(request.approvedBudget());
                event.setApprovedBy(actorId);
                event.setApprovedAt(java.time.Instant.now(clock));
                event.setRejectionReason(null);
            }
            case "REJECT" -> {
                if (request.rejectionReason() == null || request.rejectionReason().isBlank()) {
                    throw ApiException.validation("rejectionReason", "Tu choi bat buoc co ly do");
                }
                event.setStatus(EventStatus.REJECTED);
                event.setRejectionReason(request.rejectionReason().trim());
            }
            default -> throw ApiException.validation("action", "Hanh dong phai la APPROVE hoac REJECT");
        }
        suKienRepository.save(event);

        EventApprovalNotification.of(event.getId(), event.getProposedBy(), action)
                .publishWith(eventPublisher);
        return toDetail(event);
    }

    @Override
    @Transactional
    public EventDtos.ActivityCreatedResult addActivities(UUID eventId,
                                                         EventDtos.CreateActivitiesRequest request) {
        SuKien event = suKienRepository.findWithLockById(eventId)
                .orElseThrow(() -> ApiException.notFound("Su kien", eventId));
        EventAccess.requireInScope(event, accessScopePort);
        requireScheduleStatus(event);
        List<EventDtos.CreateActivityRequest> items = request.activities();
        if (items == null || items.isEmpty()) {
            throw ApiException.validation("activities", "Danh sach hoat dong rong");
        }
        UUID actorId = requireActor();

        List<HoatDongSuKien> created = new ArrayList<>();
        Set<UUID> lockedLocations = new LinkedHashSet<>();
        for (EventDtos.CreateActivityRequest item : items) {
            validateActivity(event, item);
            // Khoa vi tri dung chung mot lan per location truoc khi kiem tra/luu
            if (lockedLocations.add(item.locationId())) {
                lockScheduleLocation(item.locationId());
            }
            // Trung trong batch
            for (HoatDongSuKien existing : created) {
                if (existing.getLocation().getId().equals(item.locationId())
                        && overlaps(existing.getStartAt(), existing.getEndAt(), item.startAt(), item.endAt())) {
                    throw conflictSchedule(item.name(), existing.getStartAt(), existing.getEndAt());
                }
            }
            // Trung voi cac su kien khac dang giu cho sanh
            assertScheduleFree(item, null);

            HoatDongSuKien activity = new HoatDongSuKien();
            activity.setId(UUID.randomUUID());
            fillActivity(activity, item);
            activity.setEvent(event);
            activity.setCreatedBy(actorId);
            hoatDongRepository.save(activity);
            created.add(activity);
        }
        return new EventDtos.ActivityCreatedResult(eventId, created.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventDtos.ActivityItem> activities(UUID eventId) {
        SuKien event = suKienRepository.findById(eventId).orElseThrow(() -> ApiException.notFound("Su kien", eventId));
        EventAccess.requireInScope(event, accessScopePort);
        return hoatDongRepository.findByEventIdAndArchivedAtIsNullOrderByStartAtAsc(eventId).stream()
                .filter(activity -> accessScopePort.currentScope().allowsZone(activity.getLocation() == null
                        ? null : activity.getLocation().getZone().getId())).map(this::toActivity).toList();
    }

    @Override
    @Transactional
    public EventDtos.ActivityItem updateActivity(UUID eventId, UUID activityId, long version, EventDtos.CreateActivityRequest request) {
        HoatDongSuKien activity = requireEditableActivity(eventId, activityId, version);
        validateActivity(activity.getEvent(), request);
        lockScheduleLocation(request.locationId());
        assertScheduleFree(request, activityId);
        fillActivity(activity, request);
        hoatDongRepository.flush();
        return toActivity(activity);
    }

    @Override
    @Transactional
    public UUID archiveActivity(UUID eventId, UUID activityId, long version) {
        HoatDongSuKien activity = requireEditableActivity(eventId, activityId, version);
        activity.setArchivedAt(java.time.Instant.now(clock));
        return activityId;
    }

    private HoatDongSuKien requireEditableActivity(UUID eventId, UUID activityId, long version) {
        SuKien event = suKienRepository.findWithLockById(eventId).orElseThrow(() -> ApiException.notFound("Su kien", eventId));
        EventAccess.requireInScope(event, accessScopePort);
        requireScheduleStatus(event);
        var activity = hoatDongRepository.findById(activityId)
                .filter(item -> item.getArchivedAt() == null && eventId.equals(item.getEvent().getId()))
                .orElseThrow(() -> ApiException.notFound("Hoat dong", activityId));
        if (!accessScopePort.currentScope().allowsZone(activity.getLocation() == null ? null : activity.getLocation().getZone().getId())) {
            throw ApiException.forbidden("Hoat dong nam ngoai pham vi duoc cap quyen");
        }
        if (activity.getVersion() != version) throw ApiException.conflict("Phien ban hoat dong da thay doi");
        return activity;
    }

    private void requireScheduleStatus(SuKien event) {
        if (event.getStatus() != EventStatus.PENDING_APPROVAL && event.getStatus() != EventStatus.APPROVED) {
            throw ApiException.conflict("Chi xep lich khi su kien dang cho duyet hoac da duyet");
        }
    }

    private void validateActivity(SuKien event, EventDtos.CreateActivityRequest item) {
        if (!item.startAt().isBefore(item.endAt())) {
            throw ApiException.validation("activities", "Khung gio hoat dong khong hop le");
        }
        if (item.startAt().isBefore(event.getStartAt()) || item.endAt().isAfter(event.getEndAt())) {
            throw ApiException.validation("activities", "Hoat dong phai nam trong thoi gian su kien");
        }
        if (item.personInCharge() != null && !personLookupPort.accountExists(item.personInCharge())) {
            throw ApiException.validation("personInCharge", "Nguoi phu trach khong ton tai");
        }
    }

    private void lockScheduleLocation(UUID id) {
        if (!f01LookupPort.lockLocation(id)) throw ApiException.notFound("Vi tri", id);
        assertLocationUsable(f01LookupPort.findLocation(id).orElseThrow(() -> ApiException.notFound("Vi tri", id)), id);
    }

    private void assertScheduleFree(EventDtos.CreateActivityRequest item, UUID excludedId) {
        hoatDongRepository.findConflicts(item.locationId(), item.startAt(), item.endAt(), OCCUPYING_STATUSES).stream()
                .filter(conflict -> excludedId == null || !excludedId.equals(conflict.getId())).findFirst()
                .ifPresent(conflict -> { throw conflictSchedule(item.name(), conflict.getStartAt(), conflict.getEndAt()); });
    }

    private void fillActivity(HoatDongSuKien activity, EventDtos.CreateActivityRequest item) {
        activity.setName(item.name().trim());
        activity.setStartAt(item.startAt());
        activity.setEndAt(item.endAt());
        activity.setPersonInCharge(item.personInCharge());
        activity.setDescription(item.description());
        activity.setLocation(viTriReference(item.locationId()));
    }

    private EventDtos.ActivityItem toActivity(HoatDongSuKien activity) {
        return new EventDtos.ActivityItem(activity.getId(), activity.getName(), activity.getStartAt(), activity.getEndAt(),
                activity.getLocation() == null ? null : activity.getLocation().getId(), activity.getPersonInCharge(),
                activity.getDescription(), activity.getVersion());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EventDtos.EventSummary> search(String q, EventStatus status, int page, int size) {
        var scope = accessScopePort.currentScope();
        if (!scope.allZones() && scope.zoneIds().isEmpty()) {
            return new PageResponse<>(List.of(), page, size, 0);
        }
        String term = q == null ? "" : q.strip().toLowerCase(java.util.Locale.ROOT);
        String keyword = "%" + term.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        var results = suKienRepository.search(status, keyword, scope.allZones(), scope.zoneIds(), pageable);
        return PageResponse.of(results.map(this::toSummary));
    }

    @Override
    @Transactional(readOnly = true)
    public EventDtos.EventDetail detail(UUID eventId) {
        SuKien event = suKienRepository.findWithLocationById(eventId)
                .orElseThrow(() -> ApiException.notFound("Su kien", eventId));
        EventAccess.requireInScope(event, accessScopePort);
        return toDetail(event);
    }

    @Override
    @Transactional
    public EventDtos.ExpenseCreatedResult recordExpense(UUID eventId, EventDtos.CreateExpenseRequest request) {
        SuKien event = suKienRepository.findById(eventId)
                .orElseThrow(() -> ApiException.notFound("Su kien", eventId));
        EventAccess.requireInScope(event, accessScopePort);
        UUID actorId = requireActor();
        if (request.attachmentId() != null
                && !tepDinhKemLookupRepository.existsById(request.attachmentId())) {
            throw ApiException.notFound("Tep dinh kem", request.attachmentId());
        }
        ChiPhiThucTe expense = new ChiPhiThucTe();
        expense.setId(UUID.randomUUID());
        expense.setEvent(event);
        expense.setSupplierName(request.supplierName());
        expense.setInvoiceNumber(request.invoiceNumber());
        expense.setAmount(request.amount());
        expense.setIncurredAt(request.incurredAt() == null ? java.time.Instant.now(clock) : request.incurredAt());
        expense.setAttachmentId(request.attachmentId());
        expense.setDescription(request.description());
        expense.setCreatedBy(actorId);
        chiPhiRepository.save(expense);
        return new EventDtos.ExpenseCreatedResult(expense.getId(), expense.getAmount());
    }

    private void validateProposalFiles(List<String> keys, UUID actorId) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        if (keys.size() > maxFilesPerProposal) {
            throw ApiException.validation("proposalFileKeys",
                    "Chi duoc dinh kem toi da " + maxFilesPerProposal + " tep");
        }
        List<FileAttachmentPort.AttachmentInfo> valid = fileAttachmentPort.validateKeys(
                keys, actorId, maxFileBytes, allowedExtensions, maxFilesPerProposal);
        if (valid.size() != keys.size()) {
            throw ApiException.validation("proposalFileKeys",
                    "Tep dinh kem khong hop le (phai do nguoi gui tai len, dung dinh dang "
                            + allowedExtensions + ", <= " + maxFileBytes + " byte)");
        }
    }

    private void validateStores(List<UUID> storeIds) {
        for (UUID storeId : storeIds) {
            F01LookupPort.StoreRef store = f01LookupPort.findStore(storeId)
                    .orElseThrow(() -> ApiException.notFound("Cua hang", storeId));
            if (store.archived()) {
                throw ApiException.validation("participatingStoreIds", "Cua hang da luu tru: " + store.code());
            }
            assertZoneInScope(store.zoneId());
        }
    }

    private void assertLocationUsable(F01LookupPort.LocationRef location, UUID locationId) {
        if (location.archived()) {
            throw ApiException.validation("locationId", "Vi tri da luu tru: " + locationId);
        }
        assertZoneInScope(location.zoneId());
    }

    private void assertZoneInScope(UUID zoneId) {
        if (zoneId != null && !accessScopePort.currentScope().allowsZone(zoneId)) {
            throw ApiException.forbidden("Du lieu nam ngoai pham vi duoc cap quyen");
        }
    }

    private ViTri viTriReference(UUID locationId) {
        return entityManager.getReference(ViTri.class, locationId);
    }

    private static boolean overlaps(java.time.Instant existingStart, java.time.Instant existingEnd,
                                    java.time.Instant newStart, java.time.Instant newEnd) {
        return existingStart.isBefore(newEnd) && existingEnd.isAfter(newStart);
    }

    private static ApiException conflictSchedule(String name, java.time.Instant busyStart,
                                                 java.time.Instant busyEnd) {
        return new ApiException(ErrorCode.BUSINESS_CONFLICT,
                "Xung dot lich trinh: vi tri da co hoat dong trong khung gio "
                        + busyStart + " - " + busyEnd + " (hoat dong " + name + ")",
                List.of(), "SCHEDULE_CONFLICT");
    }

    private UUID requireActor() {
        return accessScopePort.currentActorId().orElseThrow(
                () -> new ApiException(ErrorCode.UNAUTHENTICATED, "Khong xac dinh duoc nguoi thao tac"));
    }

    private EventDtos.EventSummary toSummary(SuKien event) {
        return new EventDtos.EventSummary(event.getId(), event.getCode(), event.getName(),
                event.getStatus(), event.getEstimatedBudget(), event.getProposedBy(), event.getCreatedAt());
    }

    private EventDtos.EventDetail toDetail(SuKien event) {
        List<UUID> storeIds = suKienCuaHangRepository.findByEventId(event.getId()).stream()
                .map(SuKienCuaHang::getStoreId)
                .toList();
        String locationDescription = event.getLocation() == null ? null
                : event.getLocation().getTextDescription();
        return new EventDtos.EventDetail(event.getId(), event.getCode(), event.getName(), event.getType(),
                event.getStatus(),
                event.getLocation() == null ? null : event.getLocation().getId(),
                locationDescription, event.getStartAt(), event.getEndAt(),
                event.getEstimatedBudget(), event.getApprovedBudget(), event.getTargetAttendees(),
                event.isPublicVisible(), event.getProposedBy(), event.getApprovedBy(),
                event.getApprovedAt(), event.getRejectionReason(), storeIds, event.getVersion());
    }
}
