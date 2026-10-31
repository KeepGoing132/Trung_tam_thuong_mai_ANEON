package com.company.aneon.f01.repository;

import java.util.UUID;

import com.company.aneon.f01.entity.NganhHang;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NganhHangRepository extends JpaRepository<NganhHang, UUID> {

    boolean existsByIdAndArchivedAtIsNull(UUID id);
}
