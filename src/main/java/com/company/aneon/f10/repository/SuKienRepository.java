package com.company.aneon.f10.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import com.company.aneon.f10.entity.SuKien;
import com.company.aneon.f10.constant.EventStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface SuKienRepository extends JpaRepository<SuKien, UUID> {

    @Query("""
            select s from SuKien s left join s.location l left join l.zone z
            where s.archivedAt is null and (:status is null or s.status = :status)
              and (:allZones = true or z.id in :zoneIds)
              and (lower(s.name) like :keyword escape '!' or lower(s.code) like :keyword escape '!')
            """)
    Page<SuKien> search(@Param("status") EventStatus status, @Param("keyword") String keyword,
                         @Param("allZones") boolean allZones, @Param("zoneIds") Collection<UUID> zoneIds,
                         Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SuKien s where s.id = :id")
    Optional<SuKien> findWithLockById(@Param("id") UUID id);

    @Query("select s from SuKien s left join fetch s.location where s.id = :id")
    Optional<SuKien> findWithLocationById(@Param("id") UUID id);

    /** UNIQUE(code) khong loai archived. */
    boolean existsByCodeIgnoreCase(String code);
}
