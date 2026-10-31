package com.company.aneon.f10.repository;

import java.util.Optional;
import java.util.UUID;

import com.company.aneon.f10.entity.PhieuUuDai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface PhieuUuDaiRepository extends JpaRepository<PhieuUuDai, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from PhieuUuDai v where v.id = :id")
    Optional<PhieuUuDai> findWithLockById(@Param("id") UUID id);

    boolean existsByCodeIgnoreCase(String code);

    /**
     * Tang issued_count co dieu kien: 0 ban ghi nghia het kho (hoac xung dot).
     * issued_limit = 0 la khong gioi han (rule f10, D0006).
     */
    @Modifying
    @Query("update PhieuUuDai v set v.issuedCount = v.issuedCount + 1 "
            + "where v.id = :id and (v.issuedLimit is null or v.issuedLimit = 0 or v.issuedCount < v.issuedLimit)")
    int incrementIssuedCount(@Param("id") UUID id);

    @Modifying
    @Query("update PhieuUuDai v set v.usedCount = v.usedCount + 1 "
            + "where v.id = :id and v.usedCount < v.issuedCount")
    int incrementUsedCount(@Param("id") UUID id);
}
