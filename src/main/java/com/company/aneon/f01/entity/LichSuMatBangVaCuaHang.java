package com.company.aneon.f01.entity;

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
 * Lich su thay doi mat bang / cua hang (bang lich_su_mat_bang_cua_hang).
 * Chi them, khong sua/xoa; khong co cot version/updated_at nen khong ke
 * thua Auditable (rule 03). Target la lien ket da hinh (target_type/target_id).
 */
@Entity
@Table(name = "lich_su_mat_bang_cua_hang")
@Getter
@Setter
@NoArgsConstructor
public class LichSuMatBangVaCuaHang {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "target_type", length = 40)
    private String targetType;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "target_id", length = 36)
    private UUID targetId;

    @Column(name = "action", length = 100)
    private String action;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value_json", columnDefinition = "json")
    private String oldValueJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value_json", columnDefinition = "json")
    private String newValueJson;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "changed_by", length = 36)
    private UUID changedBy;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "created_by", length = 36)
    private UUID createdBy;

    public boolean matches(String type, UUID id) {
        return type.equals(targetType) && id.equals(targetId);
    }
}
