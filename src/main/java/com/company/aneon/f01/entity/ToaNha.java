package com.company.aneon.f01.entity;

import java.util.UUID;

import com.company.aneon.shared.persistence.Auditable;

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
 * Toa nha thuoc TTTM (bang toa_nha).
 */
@Entity
@Table(name = "toa_nha")
@Getter
@Setter
@NoArgsConstructor
public class ToaNha extends Auditable {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public ToaNha(UUID id, String code, String name, String address, UUID createdBy) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.address = address;
        setCreatedBy(createdBy);
    }
}
