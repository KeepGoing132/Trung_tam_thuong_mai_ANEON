package com.company.aneon.f10.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.company.aneon.f10.constant.PromotionStatus;
import com.company.aneon.f10.constant.PromotionType;
import com.company.aneon.shared.persistence.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Chuong trinh khuyen mai - lop cha JOINED (ba bang con chuong_trinh_*).
 * Dung JOINED theo schema, khong SINGLE_TABLE (rule 03). Khong co
 * discriminator column trong DB nen dung JOINED khong discriminator.
 */
@Entity
@Table(name = "chuong_trinh_khuyen_mai")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public abstract class ChuongTrinhKhuyenMai extends Auditable {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 40)
    private PromotionType type;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Column(name = "minimum_invoice_amount", precision = 18, scale = 2)
    private BigDecimal minimumInvoiceAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_tiers", columnDefinition = "json")
    private String targetTiers;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    private PromotionStatus status;

    @Column(name = "terms", columnDefinition = "text")
    private String terms;

    @Column(name = "public_visible", nullable = false)
    private boolean publicVisible;

    public List<String> targetTiersAsList() {
        if (targetTiers == null || targetTiers.isBlank()) {
            return List.of();
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(targetTiers,
                    new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                    });
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException("target_tiers JSON khong hop le", ex);
        }
    }

    public void setTargetTiers(List<String> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            this.targetTiers = null;
            return;
        }
        try {
            this.targetTiers = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(tiers);
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalStateException("Khong serialize duoc target_tiers", ex);
        }
    }
}
