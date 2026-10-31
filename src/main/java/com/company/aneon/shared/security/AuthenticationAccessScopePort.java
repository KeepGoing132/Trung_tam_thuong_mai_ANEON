package com.company.aneon.shared.security;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.company.aneon.shared.error.ApiException;

/**
 * Doc danh tinh va scope tu Authentication hien tai.
 * - JWT: subject = tai khoan, claim "roles", claim "zone_ids" (scope zone).
 * - AneonPrincipal (local stub): header.
 * Khong tin actorId trong JSON body (rule 05).
 */
@Component
public class AuthenticationAccessScopePort implements AccessScopePort {

    public static final String ROLES_CLAIM = "roles";
    public static final String ZONES_CLAIM = "zone_ids";

    @Override
    public Optional<UUID> currentActorId() {
        return current().map(auth -> {
            if (auth.getPrincipal() instanceof AneonPrincipal principal) {
                return principal.accountId();
            }
            if (auth.getPrincipal() instanceof Jwt jwt) {
                return UUID.fromString(jwt.getSubject());
            }
            return null;
        });
    }

    @Override
    public Set<String> currentRoles() {
        return current().map(auth -> {
            if (auth.getPrincipal() instanceof AneonPrincipal principal) {
                return principal.roles();
            }
            if (auth.getPrincipal() instanceof Jwt jwt) {
                Object roles = jwt.getClaim(ROLES_CLAIM);
                if (roles instanceof java.util.Collection<?> collection) {
                    return collection.stream().map(String::valueOf).collect(java.util.stream.Collectors.toSet());
                }
            }
            return Set.<String>of();
        }).orElseGet(Set::of);
    }

    @Override
    public AccessScope currentScope() {
        return current().map(auth -> {
            if (auth.getPrincipal() instanceof AneonPrincipal principal && principal.roles().contains("CUSTOMER")) {
                // Khach khong co scope noi bo; cac endpoint noi bo da bi chan o security layer
                return AccessScope.ALL;
            }
            java.util.List<String> rawZones = null;
            if (auth.getPrincipal() instanceof AneonPrincipal principal) {
                // Local stub truyen zone scope qua attribute cua token
                Object zones = auth.getDetails();
                if (zones instanceof java.util.List<?> list) {
                    rawZones = list.stream().map(String::valueOf).toList();
                }
            } else if (auth.getPrincipal() instanceof Jwt jwt) {
                Object zones = jwt.getClaim(ZONES_CLAIM);
                if (zones instanceof java.util.Collection<?> collection) {
                    rawZones = collection.stream().map(String::valueOf).toList();
                }
            }
            if (rawZones == null) {
                return AccessScope.ALL;
            }
            try {
                return AccessScope.ofZones(rawZones.stream().map(UUID::fromString)
                        .collect(java.util.stream.Collectors.toSet()));
            } catch (IllegalArgumentException ex) {
                throw ApiException.forbidden("Pham vi du lieu khong hop le");
            }
        }).orElseGet(() -> AccessScope.NONE);
    }

    private Optional<Authentication> current() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated);
    }
}
