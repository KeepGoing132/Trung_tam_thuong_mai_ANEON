package com.company.aneon.f10;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.company.aneon.f01.entity.KhuVuc;
import com.company.aneon.f01.entity.Tang;
import com.company.aneon.f01.entity.ToaNha;
import com.company.aneon.f01.entity.ViTri;
import com.company.aneon.f10.constant.EventStatus;
import com.company.aneon.f10.dto.EventDtos;
import com.company.aneon.f10.entity.SuKien;
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
import com.company.aneon.shared.security.AccessScope;
import com.company.aneon.shared.security.AccessScopePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventServiceImplTest {

    private SuKienRepository suKienRepository;
    private HoatDongSuKienRepository hoatDongRepository;
    private F01LookupPort f01LookupPort;
    private PersonLookupPort personLookupPort;
    private FileAttachmentPort fileAttachmentPort;
    private com.company.aneon.f10.service.EventServiceImpl service;

    private final UUID actorId = UUID.randomUUID();
    private final UUID locationId = UUID.randomUUID();
    private final UUID zoneId = UUID.randomUUID();
    private final Instant eventStart = Instant.parse("2026-11-10T09:00:00Z");
    private final Instant eventEnd = Instant.parse("2026-11-12T22:00:00Z");
    private Clock clock;

    @BeforeEach
    void setUp() {
        suKienRepository = mock(SuKienRepository.class);
        hoatDongRepository = mock(HoatDongSuKienRepository.class);
        f01LookupPort = mock(F01LookupPort.class);
        personLookupPort = mock(PersonLookupPort.class);
        fileAttachmentPort = mock(FileAttachmentPort.class);
        AccessScopePort accessScopePort = mock(AccessScopePort.class);
        when(accessScopePort.currentScope()).thenReturn(AccessScope.ALL);
        when(accessScopePort.currentActorId()).thenReturn(Optional.of(actorId));
        clock = Clock.fixed(Instant.parse("2026-10-31T00:00:00Z"), ZoneOffset.UTC);

        F01LookupPort.LocationRef location = new F01LookupPort.LocationRef(
                locationId, zoneId, "Sanh chinh tang 1", false);
        when(f01LookupPort.findLocation(locationId)).thenReturn(Optional.of(location));
        when(f01LookupPort.lockLocation(locationId)).thenReturn(true);
        when(personLookupPort.accountExists(any())).thenReturn(true);

        ToaNha building = new ToaNha(UUID.randomUUID(), "TN01", "Toa nha 1", null, actorId);
        Tang floor = new Tang();
        floor.setId(UUID.randomUUID());
        floor.setBuilding(building);
        KhuVuc zone = new KhuVuc();
        zone.setId(zoneId);
        zone.setFloor(floor);
        ViTri viTri = new ViTri();
        viTri.setId(locationId);
        viTri.setZone(zone);

        service = new com.company.aneon.f10.service.EventServiceImpl(
                suKienRepository, hoatDongRepository, mock(ChiPhiThucTeRepository.class),
                mock(SuKienCuaHangRepository.class), f01LookupPort, personLookupPort,
                fileAttachmentPort, mock(TepDinhKemLookupRepository.class), accessScopePort,
                event -> {
                }, clock, 26214400L, 10, List.of("pdf", "docx", "png", "jpg"),
                entityManagerFor(viTri));
    }

    private jakarta.persistence.EntityManager entityManagerFor(ViTri viTri) {
        jakarta.persistence.EntityManager em = mock(jakarta.persistence.EntityManager.class);
        when(em.getReference(ViTri.class, locationId)).thenReturn(viTri);
        return em;
    }

    private EventDtos.CreateEventProposalRequest proposalRequest(String code) {
        return new EventDtos.CreateEventProposalRequest(
                code, "Festival Trung Thu", "LE HOI", locationId, eventStart, eventEnd,
                new BigDecimal("150000000"), 10000, true, null, null);
    }

    @Test
    void deXuatMoiDuaVaoChoDuyet() {
        when(suKienRepository.existsByCodeIgnoreCase("EVT-001")).thenReturn(false);
        EventDtos.EventSummary summary = service.createProposal(proposalRequest("EVT-001"));
        assertThat(summary.status()).isEqualTo(EventStatus.PENDING_APPROVAL);
        assertThat(summary.proposedBy()).isEqualTo(actorId);
    }

    @Test
    void deXuatMaTrungNem409() {
        when(suKienRepository.existsByCodeIgnoreCase("EVT-001")).thenReturn(true);
        assertThatThrownBy(() -> service.createProposal(proposalRequest("EVT-001")))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.BUSINESS_CONFLICT);
    }

    @Test
    void deXuatThoiGianNguocNem400() {
        when(suKienRepository.existsByCodeIgnoreCase("EVT-002")).thenReturn(false);
        EventDtos.CreateEventProposalRequest bad = new EventDtos.CreateEventProposalRequest(
                "EVT-002", "Su kien sai", null, locationId, eventEnd, eventStart,
                BigDecimal.ONE, 0, false, null, null);
        assertThatThrownBy(() -> service.createProposal(bad))
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    private SuKien approvedEvent() {
        SuKien event = new SuKien();
        event.setId(UUID.randomUUID());
        event.setCode("EVT-001");
        event.setName("Festival");
        event.setStatus(EventStatus.PENDING_APPROVAL);
        event.setStartAt(eventStart);
        event.setEndAt(eventEnd);
        event.setProposedBy(actorId);
        event.setEstimatedBudget(new BigDecimal("100000000"));
        event.setTargetAttendees(1000);
        org.springframework.test.util.ReflectionTestUtils.setField(event, "version", 2L);
        when(suKienRepository.findWithLockById(event.getId())).thenReturn(Optional.of(event));
        return event;
    }

    @Test
    void duyetPhaiKemNganSachDuong() {
        SuKien event = approvedEvent();
        EventDtos.ApproveEventRequest request = new EventDtos.ApproveEventRequest(
                "APPROVE", null, null, null, 2L);
        assertThatThrownBy(() -> service.approve(event.getId(), request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void tuChoiBatBuocLyDo() {
        SuKien event = approvedEvent();
        EventDtos.ApproveEventRequest request = new EventDtos.ApproveEventRequest(
                "REJECT", null, null, null, 2L);
        assertThatThrownBy(() -> service.approve(event.getId(), request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void duyetGhiNganSachNguoiVaThoiDiem() {
        SuKien event = approvedEvent();
        EventDtos.EventDetail detail = service.approve(event.getId(), new EventDtos.ApproveEventRequest(
                "APPROVE", new BigDecimal("140000000"), null, "ghi chu", 2L));
        assertThat(detail.status()).isEqualTo(EventStatus.APPROVED);
        assertThat(event.getApprovedBudget()).isEqualByComparingTo(new BigDecimal("140000000"));
        assertThat(event.getApprovedBy()).isEqualTo(actorId);
        assertThat(event.getApprovedAt()).isNotNull();
    }

    @Test
    void daDuyetKhongDuyetLanNuaNem409() {
        SuKien event = approvedEvent();
        event.setStatus(EventStatus.APPROVED);
        assertThatThrownBy(() -> service.approve(event.getId(),
                new EventDtos.ApproveEventRequest("APPROVE", BigDecimal.TEN, null, null, 2L)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.BUSINESS_CONFLICT);
    }

    @Test
    void hoatDongNgoaiThoiGianSuKienNem400() {
        SuKien event = approvedEvent();
        event.setStatus(EventStatus.APPROVED);
        EventDtos.CreateActivityRequest outside = new EventDtos.CreateActivityRequest(
                "Le khai mac", eventStart.minusSeconds(3600), eventEnd, locationId, null, null);
        assertThatThrownBy(() -> service.addActivities(event.getId(),
                new EventDtos.CreateActivitiesRequest(List.of(outside))))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("trong thoi gian su kien");
    }

    @Test
    void trungLichSanhNemSCHEDULE_CONFLICT() {
        SuKien event = approvedEvent();
        event.setStatus(EventStatus.APPROVED);
        when(hoatDongRepository.findConflicts(any(), any(), any(), org.mockito.ArgumentMatchers.anyCollection()))
                .thenReturn(List.of(new com.company.aneon.f10.entity.HoatDongSuKien()));
        EventDtos.CreateActivityRequest activity = new EventDtos.CreateActivityRequest(
                "Mua lan", eventStart.plusSeconds(3600), eventEnd, locationId, null, null);
        assertThatThrownBy(() -> service.addActivities(event.getId(),
                new EventDtos.CreateActivitiesRequest(List.of(activity))))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).specificCode())
                .isEqualTo("SCHEDULE_CONFLICT");
    }

    @Test
    void nguoiPhuTrachKhongTonTaiNem400() {
        SuKien event = approvedEvent();
        event.setStatus(EventStatus.APPROVED);
        when(personLookupPort.accountExists(any())).thenReturn(false);
        EventDtos.CreateActivityRequest activity = new EventDtos.CreateActivityRequest(
                "Hoat dong", eventStart.plusSeconds(3600), eventStart.plusSeconds(7200),
                locationId, UUID.randomUUID(), null);
        assertThatThrownBy(() -> service.addActivities(event.getId(),
                new EventDtos.CreateActivitiesRequest(List.of(activity))))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("phu trach");
    }

    @Test
    void tepDinhKemKhongHopLeNem400() {
        when(suKienRepository.existsByCodeIgnoreCase("EVT-003")).thenReturn(false);
        when(fileAttachmentPort.validateKeys(anyList(), any(), any(long.class), anyList(), any(int.class)))
                .thenReturn(List.of());
        EventDtos.CreateEventProposalRequest withFile = new EventDtos.CreateEventProposalRequest(
                "EVT-003", "Su kien file sai", null, locationId, eventStart, eventEnd,
                BigDecimal.ONE, 0, false, null, List.of("khong-ton-tai.docx"));
        assertThatThrownBy(() -> service.createProposal(withFile))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("khong hop le");
    }
}
