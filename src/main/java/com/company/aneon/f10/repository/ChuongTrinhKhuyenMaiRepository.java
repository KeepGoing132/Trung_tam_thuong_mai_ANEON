package com.company.aneon.f10.repository;

import java.util.Optional;
import java.util.UUID;

import com.company.aneon.f10.entity.ChuongTrinhKhuyenMai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChuongTrinhKhuyenMaiRepository extends JpaRepository<ChuongTrinhKhuyenMai, UUID> {

    boolean existsByCodeIgnoreCase(String code);

    @Query("select p from ChuongTrinhKhuyenMai p where p.id = :id")
    Optional<ChuongTrinhKhuyenMai> findAnyById(@Param("id") UUID id);
}
