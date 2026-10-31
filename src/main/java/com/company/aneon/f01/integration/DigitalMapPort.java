package com.company.aneon.f01.integration;

import java.util.UUID;

/**
 * Port kiem tra vi tri tren ban do so F18. F01 giu location_id; F18 chi
 * xac minh kha anh xa (rule f01: khong thay location_id cua F01).
 */
public interface DigitalMapPort {

    /** false khi khong the xac minh ban do -> 503 DEPENDENCY_UNAVAILABLE. */
    boolean isAvailable();

    boolean isLocationMappable(UUID locationId);
}
