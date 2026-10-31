package com.company.aneon.f10.controller;

import java.util.UUID;

import com.company.aneon.f10.dto.PromotionDtos;
import com.company.aneon.f10.service.VoucherService;
import com.company.aneon.shared.security.RolePermissions;
import com.company.aneon.shared.web.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * F10UC02.2 + issue/redeem bo tro (D0004).
 */
@RestController
@RequestMapping("/api/v1/f10")
public class VoucherController {

    private final VoucherService voucherService;

    public VoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @PostMapping("/vouchers")
    @PreAuthorize("hasAuthority('" + RolePermissions.F10_VOUCHER_WRITE + "')")
    public ResponseEntity<ApiResponse<PromotionDtos.VoucherSummary>> create(
            @Valid @RequestBody PromotionDtos.CreateVoucherRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Khoi tao voucher thanh cong", voucherService.create(request)));
    }

    @PostMapping("/vouchers/{id}/issues")
    @PreAuthorize("hasAuthority('" + RolePermissions.F10_VOUCHER_ISSUE + "')")
    public ResponseEntity<ApiResponse<PromotionDtos.CustomerVoucherResponse>> issue(
            @PathVariable UUID id,
            @Valid @RequestBody PromotionDtos.IssueVoucherRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Phat voucher thanh cong", voucherService.issue(id, request)));
    }

    @PostMapping("/voucher-instances/{id}/redemptions")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PromotionDtos.RedeemResult> redeem(
            @PathVariable UUID id,
            @Valid @RequestBody PromotionDtos.RedeemVoucherRequest request) {
        return ApiResponse.ok("Dung voucher thanh cong", voucherService.redeem(id, request));
    }
}
