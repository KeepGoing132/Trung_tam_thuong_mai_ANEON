package com.company.aneon.f01.service;

import java.util.UUID;

import com.company.aneon.shared.security.AccessScopePort;
import com.company.aneon.shared.error.ApiException;

import org.springframework.stereotype.Component;

/**
 * Kiem tra pham vi du lieu (scope) cua mat bang theo zone.
 */
@Component
public class PremiseScopeGuard {

    private final AccessScopePort accessScopePort;

    public PremiseScopeGuard(AccessScopePort accessScopePort) {
        this.accessScopePort = accessScopePort;
    }

    public void assertZoneInScope(UUID zoneId) {
        if (zoneId != null && !accessScopePort.currentScope().allowsZone(zoneId)) {
            throw ApiException.forbidden("Du lieu nam ngoai pham vi duoc cap quyen");
        }
    }
}
