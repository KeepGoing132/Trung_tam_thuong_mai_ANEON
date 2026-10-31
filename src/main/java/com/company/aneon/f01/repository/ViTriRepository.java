package com.company.aneon.f01.repository;

import java.util.Optional;
import java.util.UUID;

import com.company.aneon.f01.entity.ViTri;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ViTriRepository extends JpaRepository<ViTri, UUID> {

    Optional<ViTri> findByIdAndZoneId(UUID id, UUID zoneId);

    Optional<ViTri> findByIdAndArchivedAtIsNull(UUID id);
}
