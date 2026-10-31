package com.company.aneon.f10.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Chuong trinh tich diem - con cua ChuongTrinhKhuyenMai (JOINED).
 */
@Entity
@Table(name = "chuong_trinh_tich_diem")
@Getter
@Setter
@NoArgsConstructor
public class ChuongTrinhTichDiem extends ChuongTrinhKhuyenMai {

    @Column(name = "reward_points")
    private Long rewardPoints;

    @Column(name = "conversion_amount", precision = 18, scale = 2)
    private java.math.BigDecimal conversionAmount;

    @Column(name = "points_per_conversion")
    private Integer pointsPerConversion;
}
