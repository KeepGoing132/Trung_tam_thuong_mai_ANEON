package com.company.aneon.f10.entity;

import java.time.Instant;
import java.util.UUID;

import com.company.aneon.f10.constant.VoucherIssueStatus;
import com.company.aneon.shared.persistence.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Voucher cua khach hang (bang phieu_uu_dai_khach_hang) - mot voucher cu
 * the da phat. Chuyen trang thai co dieu kien o VoucherService.
 */
@Entity
@Table(name = "phieu_uu_dai_khach_hang")
@Getter
@Setter
@NoArgsConstructor
public class PhieuUuDaiKhachHang extends Auditable {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voucher_id", nullable = false)
    private PhieuUuDai voucher;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "customer_id", length = 36, nullable = false, updatable = false)
    private UUID customerId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "promotion_id", length = 36)
    private UUID promotionId;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    private VoucherIssueStatus status;

    @Column(name = "redeemed_at")
    private Instant redeemedAt;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "purchase_invoice_id", length = 36)
    private UUID purchaseInvoiceId;

    @Column(name = "issue_reference", length = 200)
    private String issueReference;
}
