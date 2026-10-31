package com.company.aneon.shared.security;

import java.security.Principal;
import java.util.Set;
import java.util.UUID;

/**
 * Danh tinh noi bo cho profile local (header) va test.
 */
public record AneonPrincipal(UUID accountId, String username, Set<String> roles) implements Principal {

    @Override
    public String getName() {
        return accountId.toString();
    }
}
