package com.company.aneon.f10.repository;

import java.util.Optional;
import java.util.UUID;

import com.company.aneon.f10.entity.SuKien;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface SuKienRepository extends JpaRepository<SuKien, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SuKien s where s.id = :id")
    Optional<SuKien> findWithLockById(@Param("id") UUID id);

    @Query("select s from SuKien s left join fetch s.location where s.id = :id")
    Optional<SuKien> findWithLocationById(@Param("id") UUID id);

    /** UNIQUE(code) khong loai archived. */
    boolean existsByCodeIgnoreCase(String code);
}
