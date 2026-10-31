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
 * Lien ket nhieu-nhieu su kien - cua hang (R041). Dung association entity
 * tao id rieng, khong @ManyToMany thuan (rule 00/03).
 */
@Entity
@Table(name = "su_kien_cua_hang")
@Getter
@Setter
@NoArgsConstructor
public class SuKienCuaHang {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "event_id", length = 36, nullable = false, updatable = false)
    private UUID eventId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "store_id", length = 36, nullable = false, updatable = false)
    private UUID storeId;

    public SuKienCuaHang(UUID id, UUID eventId, UUID storeId) {
        this.id = id;
        this.eventId = eventId;
        this.storeId = storeId;
    }
}
