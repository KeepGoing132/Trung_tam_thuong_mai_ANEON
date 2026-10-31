package com.company.aneon.f01.controller;

import java.time.LocalDate;
import java.util.UUID;

import com.company.aneon.f01.dto.HistoryDtos;
import com.company.aneon.f01.service.LichSuMatBangService;
import com.company.aneon.shared.security.RolePermissions;
import com.company.aneon.shared.web.ApiResponse;
import com.company.aneon.shared.web.PageParams;
import com.company.aneon.shared.web.PageResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * F01UC04.1 - 04.2 (TK-F01 muc 5.14-5.15). Lich su chi doc.
 */
@RestController
@RequestMapping("/api/v1/f01")
public class LichSuMatBangController {

    private final LichSuMatBangService lichSuMatBangService;

    public LichSuMatBangController(LichSuMatBangService lichSuMatBangService) {
        this.lichSuMatBangService = lichSuMatBangService;
    }

    @GetMapping("/premises/{id}/history")
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_HISTORY_READ + "')")
    public ApiResponse<PageResponse<HistoryDtos.HistoryItem>> premiseHistory(
            @PathVariable UUID id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok("Lay lich su thanh cong",
                lichSuMatBangService.getPremiseHistory(id, PageParams.page(page), PageParams.size(size)));
    }

    @GetMapping("/histories")
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_HISTORY_READ + "')")
    public ApiResponse<PageResponse<HistoryDtos.HistoryItem>> search(
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) UUID targetId,
            @RequestParam(required = false) String targetCode,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) UUID changedBy,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok("Tra cuu lich su thanh cong", lichSuMatBangService.searchHistory(
                new HistoryDtos.HistorySearchRequest(targetType, targetId, targetCode, action,
                        changedBy, from, to),
                PageParams.page(page), PageParams.size(size)));
    }
}
