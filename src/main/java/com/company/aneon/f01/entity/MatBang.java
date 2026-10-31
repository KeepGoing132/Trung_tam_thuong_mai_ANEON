package com.company.aneon.f01.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.company.aneon.f01.constant.PremiseStatus;
import com.company.aneon.f01.constant.PremiseType;
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
 * Mat bang cho thue (bang mat_bang). Giu trang thai va bat bien:
 * totalArea > 0; zone va location.phai cung khu vuc; trang thai chi doi
 * qua service kiem tra hop dong.
 */
@Entity
@Table(name = "mat_bang")
@Getter
@Setter
@NoArgsConstructor
public class MatBang extends Auditable {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private KhuVuc zone;

    @Column(name = "total_area", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 40)
    private PremiseType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40, nullable = false)
    private PremiseStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private ViTri location;

    @Column(name = "description", length = 1000)
    private String description;
}
