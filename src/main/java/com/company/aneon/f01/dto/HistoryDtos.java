package com.company.aneon.f01.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO lich su mat bang/cua hang (F01UC04.x). Lich su chi doc.
 * from/to la ngay theo zone nghiep vu; service chuyen thanh Instant.
 */
public final class HistoryDtos {

    private HistoryDtos() {
    }

    public record HistoryItem(UUID id, String targetType, UUID targetId, String action,
                              String oldValueJson, String newValueJson, UUID changedBy,
                              String reason, Instant createdAt) {
    }

    public record HistorySearchRequest(String targetType, UUID targetId, String targetCode,
                                       String action, UUID changedBy, LocalDate from, LocalDate to) {
    }
}
