package com.company.aneon.f10.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.company.aneon.f10.constant.VoucherDiscountType;
import com.company.aneon.shared.persistence.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Voucher / phieu uu dai (bang phieu_uu_dai). issued_limit = 0 nghia la
 * khong gioi han, KHONG phai het kho (rule f10). MySQL la nguon cua bo dem
 * (D0006).
 */
@Entity
@Table(name = "phieu_uu_dai")
@Getter
@Setter
@NoArgsConstructor
public class PhieuUuDai extends Auditable {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 40)
    private VoucherDiscountType discountType;

    @Column(name = "value", precision = 18, scale = 2)
    private BigDecimal value;

    @Column(name = "maximum_discount", precision = 18, scale = 2)
    private BigDecimal maximumDiscount;

    @Column(name = "minimum_invoice_amount", precision = 18, scale = 2)
    private BigDecimal minimumInvoiceAmount;

    @Column(name = "issued_limit")
    private Integer issuedLimit;

    @Column(name = "issued_count", nullable = false)
    private int issuedCount;

    @Column(name = "used_count", nullable = false)
    private int usedCount;

    @Column(name = "per_customer_limit")
    private Integer perCustomerLimit;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    public boolean isUnlimitedIssue() {
        return issuedLimit == null || issuedLimit == 0;
    }

    public boolean isUnlimitedPerCustomer() {
        return perCustomerLimit == null || perCustomerLimit == 0;
    }
}
