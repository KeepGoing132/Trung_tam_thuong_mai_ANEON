package com.company.aneon.f01.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.company.aneon.f01.entity.KhuVuc;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KhuVucRepository extends JpaRepository<KhuVuc, UUID> {

    Optional<KhuVuc> findByIdAndActiveTrue(UUID id);

    List<KhuVuc> findByFloorIdAndActiveTrueOrderById(UUID floorId);
}
