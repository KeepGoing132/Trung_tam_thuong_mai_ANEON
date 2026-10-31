package com.company.aneon.f01.controller;

import java.util.UUID;

import com.company.aneon.f01.constant.PremiseStatus;
import com.company.aneon.f01.dto.PremiseDtos;
import com.company.aneon.f01.service.MatBangService;
import com.company.aneon.shared.error.ErrorCode;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.security.RolePermissions;
import com.company.aneon.shared.web.ApiResponse;
import com.company.aneon.shared.web.PageParams;
import com.company.aneon.shared.web.PageResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * F01UC01.1 - 01.5 (TK-F01 muc 5.2-5.6).
 */
@RestController
@RequestMapping("/api/v1/f01/premises")
public class MatBangController {

    private final MatBangService matBangService;

    public MatBangController(MatBangService matBangService) {
        this.matBangService = matBangService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_PREMISE_READ + "')")
    public ApiResponse<PageResponse<PremiseDtos.PremiseListItem>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID zoneId,
            @RequestParam(required = false) PremiseStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ApiResponse.ok("Tra cuu mat bang thanh cong",
                matBangService.list(q, zoneId, status, PageParams.page(page), PageParams.size(size)));
    }

    @GetMapping("/code-availability")
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_PREMISE_READ + "')")
    public ApiResponse<PremiseDtos.CodeAvailability> checkCode(
            @RequestParam String code,
            @RequestParam(required = false) UUID excludeId) {
        return ApiResponse.ok("Kiem tra ma thanh cong",
                matBangService.checkCodeAvailable(code, excludeId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_PREMISE_READ + "')")
    public ApiResponse<PremiseDtos.PremiseDetail> detail(@PathVariable UUID id) {
        return ApiResponse.ok("Tra cuu mat bang thanh cong", matBangService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_PREMISE_WRITE + "')")
    public ResponseEntity<ApiResponse<PremiseDtos.CreatePremiseResult>> create(
            @Valid @RequestBody PremiseDtos.CreatePremiseRequest request) {
        PremiseDtos.CreatePremiseResult result = matBangService.create(request);
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Them mat bang thanh cong", result));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_PREMISE_WRITE + "')")
    public ApiResponse<PremiseDtos.UpdateResult> update(@PathVariable UUID id,
                                                        @Valid @RequestBody PremiseDtos.UpdatePremiseRequest request) {
        return ApiResponse.ok("Cap nhat mat bang thanh cong", matBangService.update(id, request));
    }

    @PutMapping("/{id}/location")
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_PREMISE_WRITE + "')")
    public ApiResponse<PremiseDtos.LocationUpdateResult> relocate(
            @PathVariable UUID id,
            @Valid @RequestBody PremiseDtos.UpdatePremiseLocationRequest request) {
        return ApiResponse.ok("Cap nhat vi tri thanh cong", matBangService.relocate(id, request));
    }
}
