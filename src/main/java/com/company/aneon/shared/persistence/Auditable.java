package com.company.aneon.shared.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * Cot audit chung theo schema: created_at/created_by/updated_at/version/archived_at.
 * Chi ap tren nhung bang co day du cot; bang lich su (khong co version)
 * khong ke thua lop nay (rule 03).
 */
@MappedSuperclass
@EntityListeners(AuditListener.class)
public abstract class Auditable {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Schema dung CHAR(36): map UUID dang ky tu, khong BINARY(16). */
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "created_by", length = 36)
    private UUID createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "archived_at")
    private Instant archivedAt;

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public void setArchivedAt(Instant archivedAt) {
        this.archivedAt = archivedAt;
    }

    void setCreatedAtInstant(Instant value) {
        this.createdAt = value;
    }

    void setUpdatedAtInstant(Instant value) {
        this.updatedAt = value;
    }
}
