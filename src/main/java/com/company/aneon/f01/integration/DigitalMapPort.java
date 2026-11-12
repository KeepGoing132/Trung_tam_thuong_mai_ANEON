package com.company.aneon.f01.integration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port kiem tra vi tri tren ban do so F18. F01 giu location_id; F18 chi
 * xac minh kha anh xa (rule f01: khong thay location_id cua F01).
 */
public interface DigitalMapPort {

    record MapPoint(UUID id, BigDecimal x, BigDecimal y, String label) { }

    record FloorMap(UUID id, String name, BigDecimal minX, BigDecimal minY,
                    BigDecimal maxX, BigDecimal maxY, List<MapPoint> points) { }

    /** false khi khong the xac minh ban do -> 503 DEPENDENCY_UNAVAILABLE. */
    boolean isAvailable();

    boolean isLocationMappable(UUID locationId);

    Optional<FloorMap> findForZone(UUID zoneId);
}
