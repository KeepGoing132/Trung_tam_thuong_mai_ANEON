package com.company.aneon.shared.reference;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Tra cuu tep dinh kem chi doc theo storage_key (kiem tra so huu/dinh dang).
 */
public interface TepDinhKemLookupRepository extends JpaRepository<TepDinhKemRef, UUID> {

    Optional<TepDinhKemRef> findByStorageKey(String storageKey);
}
