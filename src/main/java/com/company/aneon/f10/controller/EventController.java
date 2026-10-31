package com.company.aneon.f10.controller;

import java.util.UUID;

import com.company.aneon.f10.dto.EventDtos;
import com.company.aneon.f10.service.EventService;
import com.company.aneon.f10.service.QrCodeService;
import com.company.aneon.shared.security.RolePermissions;
import com.company.aneon.shared.web.ApiResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * F10UC01.1/01.2/01.4/01.5 + endpoint bo tro (D0004).
 * Baseline: /proposals, /approval, /activities, /qr-code, /report.
 */
@RestController
@RequestMapping("/api/v1/f10/events")
public class EventController {

    private final EventService eventService;
    private final QrCodeService qrCodeService;
    private final com.company.aneon.f10.service.EventReportService eventReportService;

    public EventController(EventService eventService, QrCodeService qrCodeService,
                           com.company.aneon.f10.service.EventReportService eventReportService) {
        this.eventService = eventService;
        this.qrCodeService = qrCodeService;
        this.eventReportService = eventReportService;
    }

    @PostMapping("/proposals")
    @PreAuthorize("hasAuthority('" + RolePermissions.F10_EVENT_PROPOSE + "')")
    public ResponseEntity<ApiResponse<EventDtos.EventSummary>> createProposal(
            @Valid @RequestBody EventDtos.CreateEventProposalRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Tao de xuat su kien thanh cong",
                        eventService.createProposal(request)));
    }

    @PutMapping("/{id}/approval")
    @PreAuthorize("hasAuthority('" + RolePermissions.F10_EVENT_APPROVE + "')")
    public ApiResponse<EventDtos.EventDetail> approve(
            @PathVariable UUID id,
            @Valid @RequestBody EventDtos.ApproveEventRequest request) {
        return ApiResponse.ok("Xu ly duyet su kien thanh cong", eventService.approve(id, request));
    }

    @PostMapping("/{id}/activities")
    @PreAuthorize("hasAuthority('" + RolePermissions.F10_EVENT_SCHEDULE + "')")
    public ResponseEntity<ApiResponse<EventDtos.ActivityCreatedResult>> addActivities(
            @PathVariable UUID id,
            @Valid @RequestBody EventDtos.CreateActivitiesRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Xep lich hoat dong thanh cong",
                        eventService.addActivities(id, request)));
    }

    @PostMapping("/{id}/qr-code")
    @PreAuthorize("hasAuthority('" + RolePermissions.F10_EVENT_QR + "')")
    public ApiResponse<EventDtos.QrCodeDto> generateQr(@PathVariable UUID id) {
        return ApiResponse.ok("Sinh ma QR check-in thanh cong", qrCodeService.generate(id));
    }

    @PostMapping("/{id}/check-ins")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EventDtos.CheckInResult>> checkIn(
            @PathVariable UUID id,
            @Valid @RequestBody EventDtos.CheckInRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Check-in thanh cong", qrCodeService.checkIn(id, request)));
    }

    @PostMapping("/{id}/expenses")
    @PreAuthorize("hasAuthority('" + RolePermissions.F10_EXPENSE_WRITE + "')")
    public ResponseEntity<ApiResponse<EventDtos.ExpenseCreatedResult>> recordExpense(
            @PathVariable UUID id,
            @Valid @RequestBody EventDtos.CreateExpenseRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Ghi chi phi thanh cong",
                        eventService.recordExpense(id, request)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + RolePermissions.F10_EVENT_READ + "')")
    public ApiResponse<EventDtos.EventDetail> detail(@PathVariable UUID id) {
        return ApiResponse.ok("Tra cuu su kien thanh cong", eventService.detail(id));
    }

    @GetMapping("/{id}/report")
    @PreAuthorize("hasAuthority('" + RolePermissions.F10_EVENT_REPORT + "')")
    public ApiResponse<EventDtos.EventReportResponse> report(@PathVariable UUID id) {
        return ApiResponse.ok("Lay du lieu bao cao thanh cong", eventReportService.report(id));
    }
}
