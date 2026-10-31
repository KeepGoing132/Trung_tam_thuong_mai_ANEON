package com.company.aneon.f10.repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.company.aneon.f10.constant.EventStatus;
import com.company.aneon.f10.entity.HoatDongSuKien;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HoatDongSuKienRepository extends JpaRepository<HoatDongSuKien, UUID> {

    List<HoatDongSuKien> findByEventIdAndArchivedAtIsNullOrderByStartAtAsc(UUID eventId);

    /**
     * Hoat dong trung [start,end) tai cung vi tri, thuoc su kien con
     * "giu cho" (trang thai trong :statuses). Kiem tra giao khoang nua mo:
     * existing.start < new.end && existing.end > new.start (D0004).
     */
    @Query("""
            select a from HoatDongSuKien a join a.event e
            where a.location.id = :locationId
              and a.archivedAt is null and e.archivedAt is null
              and e.status in :statuses
              and a.startAt < :endAt and a.endAt > :startAt
            """)
    List<HoatDongSuKien> findConflicts(@Param("locationId") UUID locationId,
                                       @Param("startAt") Instant startAt,
                                       @Param("endAt") Instant endAt,
                                       @Param("statuses") Collection<EventStatus> statuses);
}
