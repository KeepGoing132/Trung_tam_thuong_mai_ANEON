package com.company.aneon.f01.repository;

import java.util.List;
import java.util.UUID;

import com.company.aneon.f01.entity.Tang;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TangRepository extends JpaRepository<Tang, UUID> {

    List<Tang> findByBuildingIdAndActiveTrueOrderById(UUID buildingId);
}
