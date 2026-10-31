package com.company.aneon.f01.controller;

import java.util.UUID;

import com.company.aneon.f01.constant.StoreStatus;
import com.company.aneon.f01.dto.StoreDtos;
import com.company.aneon.f01.service.CuaHangSapService;
import com.company.aneon.shared.security.RolePermissions;
import com.company.aneon.shared.web.ApiResponse;
import com.company.aneon.shared.web.PageParams;
import com.company.aneon.shared.web.PageResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * F01UC02.1 - 02.4 (TK-F01 muc 5.7-5.10).
 */
@RestController
@RequestMapping("/api/v1/f01/stores")
public class CuaHangSapController {

    private final CuaHangSapService cuaHangSapService;

    public CuaHangSapController(CuaHangSapService cuaHangSapService) {
        this.cuaHangSapService = cuaHangSapService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_STORE_READ + "')")
    public ApiResponse<PageResponse<StoreDtos.StoreListItem>> list(
            @RequestParam(required = false) UUID premiseId,
            @RequestParam(required = false) StoreStatus status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok("Tra cuu cua hang/sap thanh cong",
                cuaHangSapService.list(premiseId, status, q, PageParams.page(page), PageParams.size(size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_STORE_READ + "')")
    public ApiResponse<StoreDtos.StoreDetail> detail(@PathVariable UUID id) {
        return ApiResponse.ok("Tra cuu cua hang/sap thanh cong", cuaHangSapService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_STORE_WRITE + "')")
    public ResponseEntity<ApiResponse<StoreDtos.StoreListItem>> create(
            @Valid @RequestBody StoreDtos.CreateStoreRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Them cua hang/sap thanh cong", cuaHangSapService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_STORE_WRITE + "')")
    public ApiResponse<StoreDtos.StoreDetail> update(@PathVariable UUID id,
                                                     @Valid @RequestBody StoreDtos.UpdateStoreRequest request) {
        return ApiResponse.ok("Cap nhat cua hang/sap thanh cong", cuaHangSapService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_STORE_WRITE + "')")
    public ApiResponse<StoreDtos.StoreDetail> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StoreDtos.ChangeStoreStatusRequest request) {
        return ApiResponse.ok("Cap nhat trang thai cua hang/sap thanh cong",
                cuaHangSapService.changeStatus(id, request));
    }
}
