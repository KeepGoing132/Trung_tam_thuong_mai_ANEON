package com.company.aneon.f10.entity;

import java.time.Instant;
import java.util.UUID;

import com.company.aneon.f01.entity.ViTri;
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
 * Hoat dong trong su kien (bang hoat_dong_su_kien).
 * person_in_charge la CHAR(36) tham chieu tai khoan - khong ghi ten (rule 00).
 */
@Entity
@Table(name = "hoat_dong_su_kien")
@Getter
@Setter
@NoArgsConstructor
public class HoatDongSuKien extends Auditable {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private ViTri location;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "person_in_charge", length = 36)
    private UUID personInCharge;

    @Column(name = "description", length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private SuKien event;
}
