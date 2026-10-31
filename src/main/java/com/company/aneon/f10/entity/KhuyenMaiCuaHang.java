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
 * Lien ket nhieu-nhieu chuong trinh khuyen mai - cua hang (R039).
 */
@Entity
@Table(name = "khuyen_mai_cua_hang")
@Getter
@Setter
@NoArgsConstructor
public class KhuyenMaiCuaHang {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "promotion_id", length = 36, nullable = false, updatable = false)
    private UUID promotionId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "store_id", length = 36, nullable = false, updatable = false)
    private UUID storeId;

    public KhuyenMaiCuaHang(UUID id, UUID promotionId, UUID storeId) {
        this.id = id;
        this.promotionId = promotionId;
        this.storeId = storeId;
    }
}
