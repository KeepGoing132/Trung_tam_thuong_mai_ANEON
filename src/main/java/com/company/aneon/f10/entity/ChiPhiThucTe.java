package com.company.aneon.f10.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.company.aneon.shared.persistence.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * Chi phi thuc te cua su kien (bang chi_phi_thuc_te). attachment_id la
 * tham chieu tep_dinh_kem (dung chung), khong map association de tranh
 * phu thuoc module.
 */
@Entity
@Table(name = "chi_phi_thuc_te")
@Getter
@Setter
@NoArgsConstructor
public class ChiPhiThucTe extends Auditable {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private SuKien event;

    @Column(name = "supplier_name", length = 200)
    private String supplierName;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "incurred_at")
    private Instant incurredAt;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "attachment_id", length = 36)
    private UUID attachmentId;

    @Column(name = "description", length = 1000)
    private String description;
}
