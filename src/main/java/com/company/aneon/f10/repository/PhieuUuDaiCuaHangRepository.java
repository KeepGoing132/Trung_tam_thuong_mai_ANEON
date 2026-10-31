package com.company.aneon.f10.repository;

import java.util.List;
import java.util.UUID;

import com.company.aneon.f10.entity.PhieuUuDaiCuaHang;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PhieuUuDaiCuaHangRepository extends JpaRepository<PhieuUuDaiCuaHang, UUID> {

    List<PhieuUuDaiCuaHang> findByVoucherId(UUID voucherId);

    boolean existsByVoucherIdAndStoreId(UUID voucherId, UUID storeId);
}
