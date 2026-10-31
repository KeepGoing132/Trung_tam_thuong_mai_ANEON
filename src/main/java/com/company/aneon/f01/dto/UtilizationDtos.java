package com.company.aneon.f01.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.company.aneon.f01.constant.PremiseStatus;

/**
 * DTO khai thac mat bang (F01UC05.x). Summary tinh tren toan bo tap da loc.
 */
public final class UtilizationDtos {

    private UtilizationDtos() {
    }

    public record UtilizationSummary(long total, long occupied, long vacant, long maintenance) {
    }

    public record UtilizationItem(UUID id, String code, String name, String zoneName, String floorName,
                                  String buildingName, BigDecimal totalArea, BigDecimal allocatedArea,
                                  PremiseStatus status) {
    }

    public record UtilizationPage(UtilizationSummary summary, List<UtilizationItem> content,
                                  int page, int size, long totalElements) {
    }
}
