package com.company.aneon.f01.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.company.aneon.f01.constant.StoreStatus;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO cua hang/sap F01 (F01UC02.x).
 */
public final class StoreDtos {

    private StoreDtos() {
    }

    public record CreateStoreRequest(
            @NotBlank @Size(max = 50) @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Ma chi chua chu cai, chu so, dau cham, gach ngang") String code,
            @NotBlank @Size(max = 200) String name,
            @NotNull UUID premiseId,
            @NotNull @DecimalMin(value = "0.01", message = "Dien tich phai lon hon 0") BigDecimal area,
            UUID categoryId,
            UUID locationId,
            @NotNull StoreStatus status,
            @Size(max = 200) String operatingHours,
            @Size(max = 1000) String description,
            Boolean publicVisible) {
    }

    public record UpdateStoreRequest(
            @NotBlank @Size(max = 200) String name,
            UUID premiseId,
            @DecimalMin(value = "0.01", message = "Dien tich phai lon hon 0") BigDecimal area,
            UUID categoryId,
            UUID locationId,
            @Size(max = 200) String operatingHours,
            @Size(max = 1000) String description,
            Boolean publicVisible,
            @NotNull @Min(0) Long version) {
    }

    public record ChangeStoreStatusRequest(
            @NotNull StoreStatus status,
            @Size(max = 500) String reason,
            @NotNull @Min(0) Long version) {
    }

    public record StoreListItem(UUID id, String code, String name, UUID premiseId, String premiseCode,
                                BigDecimal area, UUID categoryId, String categoryName,
                                StoreStatus status, boolean publicVisible) {
    }

    public record StoreDetail(UUID id, String code, String name, UUID premiseId, String premiseCode,
                              BigDecimal area, UUID categoryId, String categoryName, UUID locationId,
                              StoreStatus status, String operatingHours, String description,
                              boolean publicVisible, long version) {
    }
}
