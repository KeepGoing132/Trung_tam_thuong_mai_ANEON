package com.company.aneon.f01.repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.company.aneon.f01.entity.CuaHangSap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface CuaHangSapRepository extends JpaRepository<CuaHangSap, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CuaHangSap c where c.id = :id")
    Optional<CuaHangSap> findWithLockById(@Param("id") UUID id);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, UUID id);

    /**
     * Tong dien tich cua hang dang chiem trong mat bang (khong tinh archived).
     * Day la phan bo cap cua F01; kha dung theo khoang ngay hop dong thuoc F02/F03.
     */
    @Query("select coalesce(sum(c.area), 0) from CuaHangSap c "
            + "where c.premise.id = :premiseId and c.archivedAt is null and c.area is not null")
    BigDecimal sumAllocatedArea(@Param("premiseId") UUID premiseId);

    /** Tong dien tich phan bo cho nhieu mat bang trong mot truy van (tranh N+1). */
    @Query("select c.premise.id, coalesce(sum(c.area), 0) from CuaHangSap c "
            + "where c.premise.id in :ids and c.archivedAt is null and c.area is not null "
            + "group by c.premise.id")
    java.util.List<Object[]> sumAllocatedAreas(@Param("ids") java.util.Collection<UUID> ids);

    /**
     * Tong dien tich dang phan bo voi khoa ghi (FOR UPDATE) de hai transaction
     * khong cung doc mot snapshot cu roi vuot dien tich (rule 03; MySQL
     * REPEATABLE READ: locking read thay snapshot bang du lieu moi nhat).
     */
    @Query(value = "select coalesce(sum(c.area), 0) from cua_hang c "
            + "where c.premise_id = :premiseId and c.archived_at is null and c.area is not null for update",
            nativeQuery = true)
    BigDecimal sumAllocatedAreaForUpdate(@Param("premiseId") String premiseId);
}
