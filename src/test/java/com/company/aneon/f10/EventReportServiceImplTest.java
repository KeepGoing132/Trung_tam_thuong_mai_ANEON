package com.company.aneon.f10;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.company.aneon.f10.constant.EventStatus;
import com.company.aneon.f10.dto.EventDtos;
import com.company.aneon.f10.entity.ChiPhiThucTe;
import com.company.aneon.f10.entity.SuKien;
import com.company.aneon.f10.repository.ChiPhiThucTeRepository;
import com.company.aneon.f10.repository.LuotThamGiaSuKienRepository;
import com.company.aneon.f10.repository.SuKienRepository;
import com.company.aneon.f10.service.EventReportServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventReportServiceImplTest {

    private final UUID eventId = UUID.randomUUID();
    private SuKienRepository suKienRepository;
    private ChiPhiThucTeRepository chiPhiRepository;
    private LuotThamGiaSuKienRepository attendanceRepository;
    private EventReportServiceImpl service;

    @BeforeEach
    void setUp() {
        suKienRepository = mock(SuKienRepository.class);
        chiPhiRepository = mock(ChiPhiThucTeRepository.class);
        attendanceRepository = mock(LuotThamGiaSuKienRepository.class);
        service = new EventReportServiceImpl(suKienRepository, chiPhiRepository, attendanceRepository);
    }

    private SuKien event(Integer targetAttendees, BigDecimal approvedBudget) {
        SuKien event = new SuKien();
        event.setId(eventId);
        event.setCode("EVT-001");
        event.setName("Su kien");
        event.setStatus(EventStatus.FINISHED);
        event.setTargetAttendees(targetAttendees);
        event.setApprovedBudget(approvedBudget);
        event.setEstimatedBudget(new BigDecimal("100000000"));
        when(suKienRepository.findById(eventId)).thenReturn(Optional.of(event));
        return event;
    }

    @Test
    void chenhLechNganSachDaTruChiPhi() {
        event(1000, new BigDecimal("140000000"));
        when(chiPhiRepository.sumActualExpense(eventId)).thenReturn(new BigDecimal("132500000"));
        when(attendanceRepository.countByEventId(eventId)).thenReturn(11850L);

        EventDtos.EventReportResponse report = service.report(eventId);
        assertThat(report.budgetVariance()).isEqualByComparingTo(new BigDecimal("7500000"));
        assertThat(report.attendanceRate()).isEqualByComparingTo(new BigDecimal("1185.0"));
        // ROI khong co doanh thu quy thuoc: null + trang thai, khong nhan so mau
        assertThat(report.roiPercentage()).isNull();
        assertThat(report.roiStatus()).isEqualTo("PENDING_REVENUE");
    }

    @Test
    void targetBang0KhongTinhTyLeThamDu() {
        event(0, null);
        when(chiPhiRepository.sumActualExpense(eventId)).thenReturn(BigDecimal.ZERO);
        when(attendanceRepository.countByEventId(eventId)).thenReturn(0L);
        EventDtos.EventReportResponse report = service.report(eventId);
        assertThat(report.attendanceRate()).isNull();
        assertThat(report.budgetVariance()).isNull();
        assertThat(report.roiStatus()).isEqualTo("NOT_AVAILABLE");
    }

    @Test
    void chiPhiRacBietKhongNhanBan() {
        // Tong tu repository (agregate), dem khach tu bang luot tham gia rieng
        event(500, new BigDecimal("1000000"));
        when(chiPhiRepository.sumActualExpense(eventId)).thenReturn(new BigDecimal("300000"));
        when(attendanceRepository.countByEventId(eventId)).thenReturn(120L);
        EventDtos.EventReportResponse report = service.report(eventId);
        assertThat(report.totalActualExpense()).isEqualByComparingTo(new BigDecimal("300000"));
        assertThat(report.actualAttendees()).isEqualTo(120L);
        assertThat(report.attendanceRate()).isEqualByComparingTo(new BigDecimal("24.0"));
    }
}
