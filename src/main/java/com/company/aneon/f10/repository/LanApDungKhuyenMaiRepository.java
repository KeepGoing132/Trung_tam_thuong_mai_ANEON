package com.company.aneon.f10.repository;

import java.util.Optional;
import java.util.UUID;

import com.company.aneon.f10.entity.LanApDungKhuyenMai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LanApDungKhuyenMaiRepository extends JpaRepository<LanApDungKhuyenMai, UUID> {

    Optional<LanApDungKhuyenMai> findByIdempotencyKey(String idempotencyKey);

    Optional<LanApDungKhuyenMai> findByCustomerVoucherId(UUID customerVoucherId);

    public record PromotionAggregate(long applyCount, java.math.BigDecimal totalDiscount, long totalPoints) {
    }

    /** Tong hop lan ap dung trong mot truy van, khong JOIN nhan ban. */
    @Query("select count(a), coalesce(sum(a.discountAmount), 0), coalesce(sum(a.pointsGranted), 0) "
            + "from LanApDungKhuyenMai a where a.promotionId = :promotionId")
    Object[] aggregateByPromotion(@Param("promotionId") UUID promotionId);
}
