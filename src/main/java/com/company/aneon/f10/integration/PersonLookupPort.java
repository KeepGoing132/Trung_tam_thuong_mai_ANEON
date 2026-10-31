package com.company.aneon.f10.integration;

import java.util.Optional;
import java.util.UUID;

/**
 * Port danh nghia nguoi phu trach hoat dong (tai khoan F16).
 */
public interface PersonLookupPort {

    boolean accountExists(UUID accountId);

    /** Lien ket khach hang cua tai khoan (kiem tra khach tu check-in). */
    Optional<UUID> customerLinkOf(UUID accountId);
}
