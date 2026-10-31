package com.company.aneon.f10.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.company.aneon.f10.entity.MaQrThamGia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface MaQrThamGiaRepository extends JpaRepository<MaQrThamGia, UUID> {

    /** Khoa dong dong thoi voi QR active cua su kien khi sinh/thu hoi. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from MaQrThamGia q where q.event.id = :eventId and q.active = true")
    List<MaQrThamGia> findActiveWithLockByEventId(@Param("eventId") UUID eventId);

    Optional<MaQrThamGia> findByTokenHashAndActiveTrue(String tokenHash);

    Optional<MaQrThamGia> findFirstByEventIdAndActiveTrueOrderByCreatedAtDesc(UUID eventId);

    List<MaQrThamGia> findByEventIdOrderByCreatedAtDesc(UUID eventId);
}
