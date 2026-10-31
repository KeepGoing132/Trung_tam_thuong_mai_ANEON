package com.company.aneon.f10.repository;

import java.util.List;
import java.util.UUID;

import com.company.aneon.f10.entity.SuKienCuaHang;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SuKienCuaHangRepository extends JpaRepository<SuKienCuaHang, UUID> {

    List<SuKienCuaHang> findByEventId(UUID eventId);

    boolean existsByEventIdAndStoreId(UUID eventId, UUID storeId);
}
