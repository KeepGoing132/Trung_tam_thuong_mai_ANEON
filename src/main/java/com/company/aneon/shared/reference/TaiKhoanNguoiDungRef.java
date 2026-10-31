package com.company.aneon.shared.reference;

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
 * Bang tai_khoan_nguoi_dung (F16) duoc F01/F10 tham chieu bang FK. Chi map
 * cot can thiet de tra cuu tham chieu; F16 la module ngoai pham vi trien
 * khai (rule 06) - khong CRUD, khong sua du lieu tu day.
 */
@Entity
@Table(name = "tai_khoan_nguoi_dung")
@Getter
@Setter
@NoArgsConstructor
public class TaiKhoanNguoiDungRef {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "username", length = 100, nullable = false)
    private String username;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "customer_id", length = 36)
    private UUID customerId;
}
