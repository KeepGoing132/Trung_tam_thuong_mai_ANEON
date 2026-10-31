package com.company.aneon.shared.security;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Mapping ROLE (RoleCode chung trong schema) -> quyen chuc nang.
 * Day la ten quyen DE XUAT theo docs/decisions/0008; khi F16 chot ma tran
 * quyen that thi thay bang ma cua F16, khong tao role dong nghia.
 */
public final class RolePermissions {

    public static final String F01_PREMISE_READ = "f01:premise:read";
    public static final String F01_PREMISE_WRITE = "f01:premise:write";
    public static final String F01_STORE_READ = "f01:store:read";
    public static final String F01_STORE_WRITE = "f01:store:write";
    public static final String F01_STATUS_READ = "f01:status:read";
    public static final String F01_STATUS_WRITE = "f01:status:write";
    public static final String F01_HISTORY_READ = "f01:history:read";
    public static final String F01_UTILIZATION_READ = "f01:utilization:read";

    public static final String F10_EVENT_PROPOSE = "f10:event:propose";
    public static final String F10_EVENT_APPROVE = "f10:event:approve";
    public static final String F10_EVENT_SCHEDULE = "f10:event:schedule";
    public static final String F10_EVENT_QR = "f10:event:qr";
    public static final String F10_EVENT_READ = "f10:event:read";
    public static final String F10_EVENT_REPORT = "f10:event:report";
    public static final String F10_EXPENSE_WRITE = "f10:expense:write";
    public static final String F10_CHECKIN_ASSIST = "f10:checkin:assist";
    public static final String F10_PROMOTION_READ = "f10:promotion:read";
    public static final String F10_PROMOTION_WRITE = "f10:promotion:write";
    public static final String F10_VOUCHER_WRITE = "f10:voucher:write";
    public static final String F10_VOUCHER_ISSUE = "f10:voucher:issue";

    private static final Map<String, Set<String>> ROLE_TO_PERMISSIONS = Map.of(
            "DIRECTOR", Set.of(F01_PREMISE_READ, F01_STORE_READ, F01_STATUS_READ,
                    F01_HISTORY_READ, F01_UTILIZATION_READ, F10_EVENT_APPROVE,
                    F10_EVENT_READ, F10_EVENT_REPORT, F10_PROMOTION_READ),
            "MANAGEMENT", Set.of(F01_PREMISE_READ, F01_PREMISE_WRITE, F01_STORE_READ, F01_STORE_WRITE,
                    F01_STATUS_READ, F01_STATUS_WRITE, F01_HISTORY_READ, F01_UTILIZATION_READ,
                    F10_EVENT_READ, F10_EVENT_APPROVE, F10_EVENT_REPORT, F10_PROMOTION_READ,
                    F10_PROMOTION_WRITE, F10_VOUCHER_WRITE),
            "OPERATIONS", Set.of(F01_PREMISE_READ, F01_STORE_READ, F01_STATUS_READ, F01_STATUS_WRITE,
                    F01_HISTORY_READ, F01_UTILIZATION_READ,
                    F10_CHECKIN_ASSIST, F10_VOUCHER_ISSUE),
            "ACCOUNTING", Set.of(F01_PREMISE_READ, F01_STORE_READ, F01_STATUS_READ),
            "MARKETING", Set.of(F10_EVENT_PROPOSE, F10_EVENT_SCHEDULE, F10_EVENT_QR, F10_EVENT_READ,
                    F10_EVENT_REPORT, F10_EXPENSE_WRITE, F10_PROMOTION_READ, F10_PROMOTION_WRITE,
                    F10_VOUCHER_ISSUE, F10_CHECKIN_ASSIST),
            "CUSTOMER", Set.of(),
            "TENANT", Set.of(),
            "SYSTEM_ADMIN", Set.of(F01_PREMISE_READ, F01_STORE_READ, F01_STATUS_READ, F01_HISTORY_READ,
                    F10_EVENT_READ, F10_PROMOTION_READ));

    private RolePermissions() {
    }

    public static Set<String> permissionsForRoles(Set<String> roles) {
        return roles.stream()
                .map(ROLE_TO_PERMISSIONS::get)
                .filter(java.util.Objects::nonNull)
                .flatMap(Set::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static List<org.springframework.security.core.GrantedAuthority> authoritiesForRoles(Set<String> roles) {
        return Stream.concat(
                permissionsForRoles(roles).stream().map(org.springframework.security.core.authority.SimpleGrantedAuthority::new),
                roles.stream().map(r -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + r)))
                .collect(Collectors.toList());
    }
}
