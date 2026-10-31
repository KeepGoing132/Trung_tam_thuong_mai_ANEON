package com.company.aneon.f01.dto;

import java.util.UUID;

import com.company.aneon.f01.constant.PremiseStatus;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO trang thai mat bang (F01UC03.x).
 */
public final class StatusDtos {

    private StatusDtos() {
    }

    public record ChangeStatusRequest(
            @NotNull PremiseStatus status,
            @Size(max = 500) String reason,
            @NotNull @Min(0) Long version) {
    }

    public record SyncContractStatusRequest(@NotNull UUID contractId) {
    }

    public record StatusItem(UUID id, String code, String name, PremiseStatus status) {
    }

    public record StatusChangeResult(UUID id, String code, PremiseStatus previousStatus,
                                     PremiseStatus currentStatus, long version) {
    }

    public record SyncStatusResult(String premiseCode, PremiseStatus previousStatus,
                                   PremiseStatus currentStatus) {
    }
}
