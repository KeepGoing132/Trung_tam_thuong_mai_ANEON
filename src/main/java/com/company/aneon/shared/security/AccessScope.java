package com.company.aneon.shared.security;

import java.util.Set;
import java.util.UUID;

/**
 * Pham vi du lieu cua actor: tat ca zone hoac danh sach zone duoc phep.
 * Service phai ap scope trong SQL truoc phan trang (rule 05).
 */
public record AccessScope(boolean allZones, Set<UUID> zoneIds) {

    public static final AccessScope ALL = new AccessScope(true, Set.of());

    /** Khong cho phep zone nao - dung khi khong xac dinh duoc danh tinh. */
    public static final AccessScope NONE = new AccessScope(false, Set.of());

    public boolean allowsZone(UUID zoneId) {
        if (allZones) {
            return true;
        }
        return zoneId != null && zoneIds.contains(zoneId);
    }

    public static AccessScope ofZones(Set<UUID> zoneIds) {
        if (zoneIds == null || zoneIds.isEmpty()) {
            return ALL;
        }
        return new AccessScope(false, Set.copyOf(zoneIds));
    }
}
