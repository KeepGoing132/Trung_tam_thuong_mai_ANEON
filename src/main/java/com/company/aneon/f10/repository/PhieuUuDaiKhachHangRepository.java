package com.company.aneon.f10.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.company.aneon.f10.constant.VoucherIssueStatus;
import com.company.aneon.f10.entity.PhieuUuDaiKhachHang;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface PhieuUuDaiKhachHangRepository extends JpaRepository<PhieuUuDaiKhachHang, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from PhieuUuDaiKhachHang i where i.id = :id")
    Optional<PhieuUuDaiKhachHang> findWithLockById(@Param("id") UUID id);

    long countByVoucherIdAndCustomerIdAndStatusNot(UUID voucherId, UUID customerId, VoucherIssueStatus status);

    Optional<PhieuUuDaiKhachHang> findByVoucherIdAndIssueReference(UUID voucherId, String issueReference);

    List<PhieuUuDaiKhachHang> findByCustomerIdAndStatus(UUID customerId, VoucherIssueStatus status);
}
