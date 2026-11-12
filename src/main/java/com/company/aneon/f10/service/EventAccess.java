package com.company.aneon.f10.service;

import com.company.aneon.f10.entity.SuKien;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.security.AccessScopePort;

final class EventAccess {

    private EventAccess() {
    }

    static void requireInScope(SuKien event, AccessScopePort accessScopePort) {
        if (event.getArchivedAt() != null) {
            throw ApiException.notFound("Su kien", event.getId());
        }
        var location = event.getLocation();
        var zoneId = location == null || location.getZone() == null ? null : location.getZone().getId();
        if (!accessScopePort.currentScope().allowsZone(zoneId)) {
            throw ApiException.forbidden("Su kien nam ngoai pham vi duoc cap quyen");
        }
    }
}
