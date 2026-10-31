package com.company.aneon.f01.entity;

import java.math.BigDecimal;
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
 * Toa do vi tri trong khu vuc (bang vi_tri), dung chung voi F09/F10/F18.
 */
@Entity
@Table(name = "vi_tri")
@Getter
@Setter
@NoArgsConstructor
public class ViTri extends Auditable {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private KhuVuc zone;

    @Column(name = "x", precision = 14, scale = 4)
    private BigDecimal x;

    @Column(name = "y", precision = 14, scale = 4)
    private BigDecimal y;

    @Column(name = "text_description", length = 500)
    private String textDescription;

    public boolean isMappable() {
        return x != null && y != null;
    }
}
