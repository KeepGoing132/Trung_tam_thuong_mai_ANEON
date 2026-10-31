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
 * Bang khach_mua_sam (F11): chi tra cuu ton tai khach khi check-in/phat
 * voucher (rule 06).
 */
@Entity
@Table(name = "khach_mua_sam")
@Getter
@Setter
@NoArgsConstructor
public class KhachMuaSamRef {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "full_name", length = 200, nullable = false)
    private String fullName;
}
