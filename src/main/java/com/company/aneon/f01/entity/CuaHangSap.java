package com.company.aneon.f01.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.company.aneon.f01.constant.StoreStatus;
import com.company.aneon.shared.persistence.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Cua hang / sap trong TTTM (bang cua_hang).
 * premise_id nullable theo schema de doc du lieu cu; tao moi bat buoc
 * thuoc mat bang (rule f01). Area la dien tich vat ly cua cua hang.
 */
@Entity
@Table(name = "cua_hang")
@Getter
@Setter
@NoArgsConstructor
public class CuaHangSap extends Auditable {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "premise_id")
    private MatBang premise;

    @Column(name = "area", precision = 14, scale = 2)
    private BigDecimal area;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private NganhHang category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private ViTri location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40, nullable = false)
    private StoreStatus status;

    @Column(name = "operating_hours", length = 200)
    private String operatingHours;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "public_visible", nullable = false)
    private boolean publicVisible;

    public boolean isPublic() {
        // Cong khai chi khi co co hieu; bo loc trang thai hien hanh nam o truy van
        return publicVisible && getArchivedAt() == null;
    }
}
