package com.company.aneon.f01.controller;

import java.util.UUID;

import com.company.aneon.f01.constant.PremiseStatus;
import com.company.aneon.f01.dto.StatusDtos;
import com.company.aneon.f01.service.TrangThaiMatBangService;
import com.company.aneon.shared.security.RolePermissions;
import com.company.aneon.shared.web.ApiResponse;
import com.company.aneon.shared.web.PageParams;
import com.company.aneon.shared.web.PageResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * F01UC03.1 - 03.3 (TK-F01 muc 5.11-5.13). Path tinh /statuses khac /{id}.
 */
@RestController
@RequestMapping("/api/v1/f01/premises")
public class TrangThaiMatBangController {

    private final TrangThaiMatBangService trangThaiMatBangService;

    public TrangThaiMatBangController(TrangThaiMatBangService trangThaiMatBangService) {
        this.trangThaiMatBangService = trangThaiMatBangService;
    }

    @GetMapping("/statuses")
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_STATUS_READ + "')")
    public ApiResponse<PageResponse<StatusDtos.StatusItem>> statuses(
            @RequestParam(required = false) PremiseStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok("Lay trang thai mat bang thanh cong",
                trangThaiMatBangService.getCurrentStatuses(status, PageParams.page(page), PageParams.size(size)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_STATUS_WRITE + "')")
    public ApiResponse<StatusDtos.StatusChangeResult> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusDtos.ChangeStatusRequest request) {
        return ApiResponse.ok("Cap nhat trang thai mat bang thanh cong",
                trangThaiMatBangService.updatePremiseStatus(id, request));
    }

    @PostMapping("/{id}/sync-contract-status")
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_STATUS_WRITE + "')")
    public ApiResponse<StatusDtos.SyncStatusResult> syncContractStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusDtos.SyncContractStatusRequest request) {
        return ApiResponse.ok("Dong bo trang thai thanh cong",
                trangThaiMatBangService.syncStatusFromContract(id, request));
    }
}
