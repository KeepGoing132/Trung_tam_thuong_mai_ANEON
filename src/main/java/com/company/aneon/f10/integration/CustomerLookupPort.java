package com.company.aneon.f10.integration;

import java.util.UUID;

/**
 * Port khach hang F11 (chi tra cuu ton tai).
 */
public interface CustomerLookupPort {

    boolean customerExists(UUID customerId);
}
