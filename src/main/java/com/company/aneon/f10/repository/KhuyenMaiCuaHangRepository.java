package com.company.aneon.f10.repository;

import java.util.List;
import java.util.UUID;

import com.company.aneon.f10.entity.KhuyenMaiCuaHang;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KhuyenMaiCuaHangRepository extends JpaRepository<KhuyenMaiCuaHang, UUID> {

    List<KhuyenMaiCuaHang> findByPromotionId(UUID promotionId);
}
