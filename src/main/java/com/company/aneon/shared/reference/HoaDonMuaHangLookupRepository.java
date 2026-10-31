package com.company.aneon.shared.reference;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Tra cuu hoa don mua hang F11 chi doc (nguon so tien xac minh).
 */
public interface HoaDonMuaHangLookupRepository extends JpaRepository<HoaDonMuaHangRef, UUID> {
}
