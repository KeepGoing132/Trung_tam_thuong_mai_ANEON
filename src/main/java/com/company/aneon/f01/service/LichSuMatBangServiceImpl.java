package com.company.aneon.f01.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.UUID;

import com.company.aneon.f01.constant.HistoryTargetType;
import com.company.aneon.f01.dto.HistoryDtos;
import com.company.aneon.f01.entity.LichSuMatBangVaCuaHang;
import com.company.aneon.f01.repository.LichSuMatBangVaCuaHangRepository;
import com.company.aneon.f01.repository.MatBangRepository;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.security.AccessScopePort;
import com.company.aneon.shared.web.PageResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * F01UC04.1 - 04.2: lich su chi doc, sort created_at giam dan.
 */
@Service
public class LichSuMatBangServiceImpl implements LichSuMatBangService {

    private final LichSuMatBangVaCuaHangRepository historyRepository;
    private final MatBangRepository matBangRepository;
    private final AccessScopePort accessScopePort;
    private final ZoneId businessZone;

    public LichSuMatBangServiceImpl(LichSuMatBangVaCuaHangRepository historyRepository,
                                    MatBangRepository matBangRepository, AccessScopePort accessScopePort,
                                    ZoneId businessZone) {
        this.historyRepository = historyRepository;
        this.matBangRepository = matBangRepository;
        this.accessScopePort = accessScopePort;
        this.businessZone = businessZone;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HistoryDtos.HistoryItem> getPremiseHistory(UUID premiseId, int page, int size) {
        var premise = matBangRepository.findWithZoneFloorById(premiseId)
                .orElseThrow(() -> ApiException.notFound("Mat bang", premiseId));
        if (!accessScopePort.currentScope().allowsZone(premise.getZone().getId())) {
            throw ApiException.forbidden("Du lieu nam ngoai pham vi duoc cap quyen");
        }
        Page<LichSuMatBangVaCuaHang> result = historyRepository.findTargetHistory(
                HistoryTargetType.MAT_BANG, premiseId, PageRequest.of(page, size));
        return PageResponse.of(result.map(this::toItem));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HistoryDtos.HistoryItem> searchHistory(HistoryDtos.HistorySearchRequest request,
                                                               int page, int size) {
        if (request.from() != null && request.to() != null && request.from().isAfter(request.to())) {
            throw ApiException.validation("from", "Khoang ngay tu > den khong hop le");
        }
        String targetType = request.targetType();
        if (targetType != null && !targetType.equals(HistoryTargetType.MAT_BANG)
                && !targetType.equals(HistoryTargetType.CUA_HANG)) {
            throw ApiException.validation("targetType", "Loai doi tuong khong hop le");
        }
        Instant fromTime = request.from() == null ? null : startOfDay(request.from());
        Instant toTime = request.to() == null ? null : endOfDayInclusive(request.to());
        var scope = accessScopePort.currentScope();
        // Tranh IN rong: khong co zone nao duoc phep thi tra trang rong
        if (!scope.allZones() && scope.zoneIds().isEmpty()) {
            return new PageResponse<>(java.util.List.of(), page, size, 0);
        }
        Page<LichSuMatBangVaCuaHang> result = historyRepository.searchHistory(
                targetType, request.targetId(), request.action(), request.changedBy(),
                fromTime, toTime, request.targetCode(),
                scope.allZones(),
                scope.allZones() ? java.util.List.of(UUID.randomUUID()) : scope.zoneIds(),
                PageRequest.of(page, size));
        return PageResponse.of(result.map(this::toItem));
    }

    private Instant startOfDay(LocalDate date) {
        return date.atStartOfDay(businessZone).toInstant();
    }

    private Instant endOfDayInclusive(LocalDate date) {
        return date.plusDays(1).atStartOfDay(businessZone).toInstant().minusNanos(1);
    }

    private HistoryDtos.HistoryItem toItem(LichSuMatBangVaCuaHang entry) {
        return new HistoryDtos.HistoryItem(entry.getId(), entry.getTargetType(), entry.getTargetId(),
                entry.getAction(), entry.getOldValueJson(), entry.getNewValueJson(),
                entry.getChangedBy(), entry.getReason(), entry.getCreatedAt());
    }
}
