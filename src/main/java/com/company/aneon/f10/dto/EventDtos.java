package com.company.aneon.f10.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.company.aneon.f10.constant.EventStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO su kien F10 (F10UC01.x). Client khong duoc gan proposedBy/approvedBy/
 * approvedBudget/ngan sach duyet (rule 04).
 */
public final class EventDtos {

    private EventDtos() {
    }

    public record CreateEventProposalRequest(
            @NotBlank @Size(max = 50) @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Ma chi chua chu cai, chu so, dau cham, gach ngang") String code,
            @NotBlank @Size(max = 200) String name,
            @Size(max = 100) String type,
            @NotNull UUID locationId,
            @NotNull Instant startAt,
            @NotNull Instant endAt,
            @NotNull @DecimalMin(value = "0", message = "Ngan sach du kien khong am") BigDecimal estimatedBudget,
            @NotNull @Min(0) Integer targetAttendees,
            Boolean publicVisible,
            List<UUID> participatingStoreIds,
            List<String> proposalFileKeys) {
    }

    public record ApproveEventRequest(
            @NotBlank String action,
            @DecimalMin(value = "0", message = "Ngan sach duyet khong am") BigDecimal approvedBudget,
            @Size(max = 500) String rejectionReason,
            @Size(max = 500) String note,
            @NotNull @Min(0) Long version) {
    }

    public record CreateActivityRequest(
            @NotBlank @Size(max = 200) String name,
            @NotNull Instant startAt,
            @NotNull Instant endAt,
            @NotNull UUID locationId,
            UUID personInCharge,
            @Size(max = 1000) String description) {
    }

    public record CreateActivitiesRequest(@NotNull List<@NotNull @Valid CreateActivityRequest> activities) {
    }

    public record ActivityItem(UUID id, String name, Instant startAt, Instant endAt, UUID locationId,
                               UUID personInCharge, String description, long version) { }

    public record CreateExpenseRequest(
            @Size(max = 200) String supplierName,
            @Size(max = 100) String invoiceNumber,
            @NotNull @DecimalMin(value = "0.01", message = "So tien phai lon hon 0") BigDecimal amount,
            Instant incurredAt,
            UUID attachmentId,
            @Size(max = 1000) String description) {
    }

    public record QrCodeDto(UUID qrCodeId, String checkInUrl, String imageStorageKey,
                            Instant expiresAt, boolean active) {
    }

    public record CheckInRequest(String qrToken,
                                 UUID customerId,
                                 @Size(max = 200) String scanningDevice) {
    }

    public record EventSummary(UUID id, String code, String name, EventStatus status,
                               BigDecimal estimatedBudget, UUID proposedBy, Instant createdAt) {
    }

    public record EventDetail(UUID id, String code, String name, String type, EventStatus status,
                              UUID locationId, String locationDescription, Instant startAt, Instant endAt,
                              BigDecimal estimatedBudget, BigDecimal approvedBudget, Integer targetAttendees,
                              boolean publicVisible, UUID proposedBy, UUID approvedBy, Instant approvedAt,
                              String rejectionReason, List<UUID> participatingStoreIds, long version) {
    }

    public record ActivityCreatedResult(UUID eventId, int createdCount) {
    }

    public record ExpenseCreatedResult(UUID id, java.math.BigDecimal amount) {
    }

    public record CheckInResult(UUID checkInId, UUID eventId, UUID customerId, Instant checkInAt,
                                boolean duplicate) {
    }

    public record EventReportResponse(UUID eventId, String eventName, EventStatus status,
                                      BigDecimal approvedBudget, BigDecimal estimatedBudget,
                                      BigDecimal totalActualExpense, BigDecimal budgetVariance,
                                      Integer targetAttendees, long actualAttendees,
                                      BigDecimal attendanceRate, BigDecimal roiPercentage,
                                      String roiStatus) {
    }
}
