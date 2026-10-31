package com.company.aneon.f10.entity;

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
 * Lien ket nhieu-nhieu voucher - cua hang (R040).
 */
@Entity
@Table(name = "phieu_uu_dai_cua_hang")
@Getter
@Setter
@NoArgsConstructor
public class PhieuUuDaiCuaHang {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "voucher_id", length = 36, nullable = false, updatable = false)
    private UUID voucherId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "store_id", length = 36, nullable = false, updatable = false)
    private UUID storeId;

    public PhieuUuDaiCuaHang(UUID id, UUID voucherId, UUID storeId) {
        this.id = id;
        this.voucherId = voucherId;
        this.storeId = storeId;
    }
}
