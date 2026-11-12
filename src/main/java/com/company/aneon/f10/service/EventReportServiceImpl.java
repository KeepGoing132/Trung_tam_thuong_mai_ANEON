package com.company.aneon.f10.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import com.company.aneon.f10.dto.EventDtos;
import com.company.aneon.f10.entity.SuKien;
import com.company.aneon.f10.repository.ChiPhiThucTeRepository;
import com.company.aneon.f10.repository.LuotThamGiaSuKienRepository;
import com.company.aneon.f10.repository.SuKienRepository;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.security.AccessScopePort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * F10UC01.4. Tong chi phi tu ban ghi thuc te; chenh lech = approvedBudget -
 * totalActualExpense; dem khach du nhat; attendanceRate chi khi target > 0;
 * ROI null khi thieu doanh thu quy thuoc (khong nhan so mau, rule f10).
 */
@Service
public class EventReportServiceImpl implements EventReportService {

    private final SuKienRepository suKienRepository;
    private final ChiPhiThucTeRepository chiPhiRepository;
    private final LuotThamGiaSuKienRepository attendanceRepository;
    private final AccessScopePort accessScopePort;

    public EventReportServiceImpl(SuKienRepository suKienRepository,
                                  ChiPhiThucTeRepository chiPhiRepository,
                                  LuotThamGiaSuKienRepository attendanceRepository,
                                  AccessScopePort accessScopePort) {
        this.suKienRepository = suKienRepository;
        this.chiPhiRepository = chiPhiRepository;
        this.attendanceRepository = attendanceRepository;
        this.accessScopePort = accessScopePort;
    }

    @Override
    @Transactional(readOnly = true)
    public EventDtos.EventReportResponse report(UUID eventId) {
        SuKien event = suKienRepository.findById(eventId)
                .orElseThrow(() -> ApiException.notFound("Su kien", eventId));
        EventAccess.requireInScope(event, accessScopePort);

        BigDecimal totalExpense = chiPhiRepository.sumActualExpense(eventId);
        BigDecimal budgetVariance = event.getApprovedBudget() == null ? null
                : event.getApprovedBudget().subtract(totalExpense);

        long actualAttendees = attendanceRepository.countByEventId(eventId);
        BigDecimal attendanceRate = null;
        if (event.getTargetAttendees() != null && event.getTargetAttendees() > 0) {
            attendanceRate = BigDecimal.valueOf(actualAttendees * 100L)
                    .divide(BigDecimal.valueOf(event.getTargetAttendees()), 1, RoundingMode.HALF_UP);
        }

        // Doanh thu quy thuoc chua co nguon xac dinh (D0004): khong tinh ROI, tra trang thai
        String roiStatus = totalExpense.signum() > 0 ? "PENDING_REVENUE" : "NOT_AVAILABLE";
        return new EventDtos.EventReportResponse(event.getId(), event.getName(), event.getStatus(),
                event.getApprovedBudget(), event.getEstimatedBudget(), totalExpense, budgetVariance,
                event.getTargetAttendees(), actualAttendees, attendanceRate, null, roiStatus);
    }
}
