package com.company.aneon.f01.repository;

import java.util.UUID;

import com.company.aneon.f01.entity.LichSuMatBangVaCuaHang;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface LichSuMatBangVaCuaHangRepository extends JpaRepository<LichSuMatBangVaCuaHang, UUID> {

    @Query("""
            select h from LichSuMatBangVaCuaHang h
            where h.targetType = :targetType and h.targetId = :targetId
            order by h.createdAt desc, h.id desc
            """)
    Page<LichSuMatBangVaCuaHang> findTargetHistory(@Param("targetType") String targetType,
                                                   @Param("targetId") UUID targetId, Pageable pageable);

    /**
     * Tra cuu lich su theo tieu chi; join mem den mat bang / cua hang de
     * loc theo code va ap scope trong SQL truoc phan trang (rule 05).
     */
    @Query("""
            select h from LichSuMatBangVaCuaHang h
            left join MatBang mb on h.targetType = 'MAT_BANG' and mb.id = h.targetId
            left join CuaHangSap ch on h.targetType = 'CUA_HANG' and ch.id = h.targetId
            where (:targetType is null or h.targetType = :targetType)
              and (:targetId is null or h.targetId = :targetId)
              and (:action is null or h.action = :action)
              and (:changedBy is null or h.changedBy = :changedBy)
              and (:fromTime is null or h.createdAt >= :fromTime)
              and (:toTime is null or h.createdAt <= :toTime)
              and (:targetCode is null or mb.code = :targetCode or ch.code = :targetCode)
              and (:scopeAll = true or mb.zone.id in :zoneIds or ch.premise.zone.id in :zoneIds)
            """)
    Page<LichSuMatBangVaCuaHang> searchHistory(@Param("targetType") String targetType,
                                               @Param("targetId") UUID targetId,
                                               @Param("action") String action,
                                               @Param("changedBy") UUID changedBy,
                                               @Param("fromTime") Instant fromTime,
                                               @Param("toTime") Instant toTime,
                                               @Param("targetCode") String targetCode,
                                               @Param("scopeAll") boolean scopeAll,
                                               @Param("zoneIds") java.util.Collection<UUID> zoneIds,
                                               Pageable pageable);
}
