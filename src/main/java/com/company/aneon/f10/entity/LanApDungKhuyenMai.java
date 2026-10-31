package com.company.aneon.f10.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lan ap dung khuyen mai cho hoa don (bang lan_ap_dung_khuyen_mai).
 * idempotency_key co UNIQUE o V2 - cung key khac payload la xung dot
 * (rule 03). Bang khong co version/updated_at.
 */
@Entity
@Table(name = "lan_ap_dung_khuyen_mai")
@Getter
@Setter
@NoArgsConstructor
public class LanApDungKhuyenMai {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "promotion_id", length = 36, nullable = false, updatable = false)
    private UUID promotionId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "customer_id", length = 36, nullable = false, updatable = false)
    private UUID customerId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "invoice_id", length = 36, nullable = false, updatable = false)
    private UUID invoiceId;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "points_granted")
    private Long pointsGranted;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "customer_voucher_id", length = 36)
    private UUID customerVoucherId;

    @Column(name = "idempotency_key", length = 120)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "created_by", length = 36)
    private UUID createdBy;
}
