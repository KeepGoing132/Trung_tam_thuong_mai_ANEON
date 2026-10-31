package com.company.aneon.f01.repository;

import java.util.Optional;
import java.util.UUID;

import com.company.aneon.f01.entity.MatBang;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface MatBangRepository extends JpaRepository<MatBang, UUID> {

    /**
     * Khoa dong hang mat bang truoc khi tinh tong dien tich / sua thong tin,
     * de hai request khong the cung vuot dien tich (rule 03).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from MatBang m where m.id = :id")
    Optional<MatBang> findWithLockById(@Param("id") UUID id);

    /** UNIQUE(code) khong loai archived nen kiem tra tren moi ban ghi. */
    boolean existsByCodeIgnoreCase(String code);

    java.util.Optional<MatBang> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    @Query("select m from MatBang m left join fetch m.zone z left join fetch z.floor where m.id = :id")
    Optional<MatBang> findWithZoneFloorById(@Param("id") UUID id);
}
