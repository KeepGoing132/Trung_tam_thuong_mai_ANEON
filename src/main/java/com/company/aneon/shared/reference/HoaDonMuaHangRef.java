package com.company.aneon.shared.reference;

import java.math.BigDecimal;
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
 * Bang hoa_don_mua_hang (F11): nguon xac minh so tien that khi ap dung
 * khuyen mai - khong tin so tien client khai (rule f10).
 */
@Entity
@Table(name = "hoa_don_mua_hang")
@Getter
@Setter
@NoArgsConstructor
public class HoaDonMuaHangRef {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "number", length = 50, nullable = false)
    private String number;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "customer_id", length = 36, nullable = false, updatable = false)
    private UUID customerId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "store_id", length = 36, nullable = false, updatable = false)
    private UUID storeId;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "verified", nullable = false)
    private boolean verified;
}
