package com.company.aneon.f10.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Chuong trinh giam gia - con cua ChuongTrinhKhuyenMai (JOINED).
 * discount_rate theo thang 0..100 (API nhan phan tram, D0004).
 */
@Entity
@Table(name = "chuong_trinh_giam_gia")
@Getter
@Setter
@NoArgsConstructor
public class ChuongTrinhGiamGia extends ChuongTrinhKhuyenMai {

    @Column(name = "discount_rate", precision = 9, scale = 4)
    private BigDecimal discountRate;

    @Column(name = "maximum_discount", precision = 18, scale = 2)
    private BigDecimal maximumDiscount;
}
