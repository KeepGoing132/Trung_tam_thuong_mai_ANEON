package com.company.aneon.f10.controller;

import java.util.UUID;

import com.company.aneon.f10.dto.PromotionDtos;
import com.company.aneon.f10.service.PromotionService;
import com.company.aneon.shared.security.RolePermissions;
import com.company.aneon.shared.web.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * F10UC02.1/02.3 (TK-F10 bang tong hop) + apply bo tro (D0004).
 */
@RestController
@RequestMapping("/api/v1/f10/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + RolePermissions.F10_PROMOTION_WRITE + "')")
    public ResponseEntity<ApiResponse<PromotionDtos.PromotionSummary>> create(
            @Valid @RequestBody PromotionDtos.CreatePromotionRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Tao chuong trinh khuyen mai thanh cong",
                        promotionService.create(request)));
    }

    @GetMapping("/{id}/realtime")
    @PreAuthorize("hasAuthority('" + RolePermissions.F10_PROMOTION_READ + "')")
    public ApiResponse<PromotionDtos.PromotionRealtimeResponse> realtime(@PathVariable UUID id) {
        return ApiResponse.ok("Lay so lieu thoi gian thuc thanh cong",
                promotionService.realtime(id));
    }

    @PostMapping("/{id}/applications")
    @PreAuthorize("hasAnyRole('MARKETING','OPERATIONS')")
    public ResponseEntity<ApiResponse<PromotionDtos.ApplyPromotionResult>> apply(
            @PathVariable UUID id,
            @Valid @RequestBody PromotionDtos.ApplyPromotionRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Ap dung khuyen mai thanh cong",
                        promotionService.apply(id, request)));
    }
}
