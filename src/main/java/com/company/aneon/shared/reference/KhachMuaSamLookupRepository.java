package com.company.aneon.shared.reference;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Tra cuu khach mua sam F11 chi doc.
 */
public interface KhachMuaSamLookupRepository extends JpaRepository<KhachMuaSamRef, UUID> {
}
