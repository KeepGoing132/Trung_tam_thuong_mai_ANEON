package com.company.aneon.shared.security;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Port danh tinh/scope theo rule 06: F16 la phu thuoc, module F01/F10 khong
 * truy van repository F16 truc tiep. Impl gan Authentication hien tai.
 */
public interface AccessScopePort {

    /** UUID tai khoan dang thao tac (lay tu danh tinh xac thuc, khong tin body). */
    Optional<UUID> currentActorId();

    /** Cac vai tro cua actor hien tai. */
    Set<String> currentRoles();

    /** Pham vi zone duoc phep; khong co gioi han thi ALL. */
    AccessScope currentScope();
}
