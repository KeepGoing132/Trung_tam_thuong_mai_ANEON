package com.company.aneon.f10.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Chuong trinh tang voucher - con cua ChuongTrinhKhuyenMai (JOINED).
 */
@Entity
@Table(name = "chuong_trinh_tang_phieu")
@Getter
@Setter
@NoArgsConstructor
public class ChuongTrinhTangPhieuUuDai extends ChuongTrinhKhuyenMai {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id")
    private PhieuUuDai voucher;

    @Column(name = "total_to_issue")
    private Integer totalToIssue;

    @Column(name = "issued_count", nullable = false)
    private int issuedCount;
}
