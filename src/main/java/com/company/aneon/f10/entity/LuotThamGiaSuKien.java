package com.company.aneon.f10.entity;

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
 * Luot tham gia su kien (bang luot_tham_gia_su_kien). Bang khong co
 * version/updated_at nen khong ke thua Auditable (rule 03). UNIQUE
 * (event_id, customer_id) o V2 chong check-in lap.
 */
@Entity
@Table(name = "luot_tham_gia_su_kien")
@Getter
@Setter
@NoArgsConstructor
public class LuotThamGiaSuKien {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "event_id", length = 36, nullable = false, updatable = false)
    private UUID eventId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "customer_id", length = 36, nullable = false, updatable = false)
    private UUID customerId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "qr_code_id", length = 36)
    private UUID qrCodeId;

    @Column(name = "check_in_at")
    private Instant checkInAt;

    @Column(name = "scanning_device", length = 200)
    private String scanningDevice;

    @Column(name = "reward_reference", length = 200)
    private String rewardReference;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "created_by", length = 36)
    private UUID createdBy;
}
