package com.company.aneon.f10.entity;

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
 * Ma QR tham gia su kien (bang ma_qr_tham_gia). Chi luu hash token
 * (SHA-256), khong luu token goc; check_in_url chua token goc nen la bi
 * mat tuong duong raw token - khong log, khong tra trong DTO cong khai
 * (rule 05, D0007).
 */
@Entity
@Table(name = "ma_qr_tham_gia")
@Getter
@Setter
@NoArgsConstructor
public class MaQrThamGia extends Auditable {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private SuKien event;

    @Column(name = "token_hash", length = 128, nullable = false)
    private String tokenHash;

    @Column(name = "check_in_url", length = 512)
    private String checkInUrl;

    @Column(name = "image_storage_key", length = 300)
    private String imageStorageKey;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "active", nullable = false)
    private boolean active;

    public boolean isActive() {
        return active;
    }
}
