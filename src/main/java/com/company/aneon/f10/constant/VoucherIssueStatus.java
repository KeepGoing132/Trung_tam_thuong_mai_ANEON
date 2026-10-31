package com.company.aneon.f10.constant;

/**
 * Trang thai voucher cua khach hang (PlantUML VoucherIssueStatus).
 * Chuyen co dieu kien: ISSUED -> USED/REVOKED/EXPIRED; khong redeem hai lan.
 */
public enum VoucherIssueStatus {
    ISSUED,
    USED,
    EXPIRED,
    REVOKED
}
