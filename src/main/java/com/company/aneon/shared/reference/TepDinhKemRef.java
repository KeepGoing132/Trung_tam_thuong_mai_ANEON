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
 * Bang tep_dinh_kem dung chung: chi doc de kiem tra key tep co hop le
 * (so huu, dung luong, dinh dang) - khong CRUD (rule 05).
 */
@Entity
@Table(name = "tep_dinh_kem")
@Getter
@Setter
@NoArgsConstructor
public class TepDinhKemRef {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "storage_key", length = 300)
    private String storageKey;

    @Column(name = "original_name", length = 300)
    private String originalName;

    @Column(name = "media_type", length = 100)
    private String mediaType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "uploaded_by", length = 36)
    private UUID uploadedBy;
}
