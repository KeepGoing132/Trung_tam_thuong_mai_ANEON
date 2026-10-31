package com.company.aneon.f10.repository;

import java.util.Optional;
import java.util.UUID;

import com.company.aneon.f10.entity.LuotThamGiaSuKien;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LuotThamGiaSuKienRepository extends JpaRepository<LuotThamGiaSuKien, UUID> {

    Optional<LuotThamGiaSuKien> findByEventIdAndCustomerId(UUID eventId, UUID customerId);

    long countByEventId(UUID eventId);
}
