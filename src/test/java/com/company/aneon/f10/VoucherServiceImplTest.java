package com.company.aneon.f10;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.company.aneon.f10.constant.PromotionType;
import com.company.aneon.f10.dto.PromotionDtos;
import com.company.aneon.f10.entity.ChuongTrinhGiamGia;
import com.company.aneon.f10.entity.PhieuUuDai;
import com.company.aneon.f10.entity.PhieuUuDaiKhachHang;
import com.company.aneon.f10.integration.CustomerLookupPort;
import com.company.aneon.f10.integration.F01LookupPort;
import com.company.aneon.f10.integration.InvoiceLookupPort;
import com.company.aneon.f10.integration.PersonLookupPort;
import com.company.aneon.f10.repository.LanApDungKhuyenMaiRepository;
import com.company.aneon.f10.repository.PhieuUuDaiCuaHangRepository;
import com.company.aneon.f10.repository.PhieuUuDaiKhachHangRepository;
import com.company.aneon.f10.repository.PhieuUuDaiRepository;
import com.company.aneon.f10.service.PromotionServiceImpl;
import com.company.aneon.f10.service.VoucherServiceImpl;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.error.ErrorCode;
import com.company.aneon.shared.security.AccessScope;
import com.company.aneon.shared.security.AccessScopePort;
import com.company.aneon.shared.security.LocalIdentityFilter;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Kho voucher: issuedLimit=0 la khong gioi han; phat voucher cuoi cung
 * canh tranh; idempotency theo issueReference; redeem ISSUED->USED.
 */
class VoucherServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-01T00:00:00Z"), ZoneOffset.UTC);

    private PhieuUuDaiRepository voucherRepository;
    private PhieuUuDaiKhachHangRepository instanceRepository;
    private InvoiceLookupPort invoiceLookupPort;
    private VoucherServiceImpl service;

    private final UUID actorId = UUID.randomUUID();
    private final UUID voucherId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final UUID instanceId = UUID.randomUUID();
    private final UUID invoiceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        voucherRepository = mock(PhieuUuDaiRepository.class);
        instanceRepository = mock(PhieuUuDaiKhachHangRepository.class);
        invoiceLookupPort = mock(InvoiceLookupPort.class);
        CustomerLookupPort customerLookupPort = mock(CustomerLookupPort.class);
        when(customerLookupPort.customerExists(any())).thenReturn(true);
        PersonLookupPort personLookupPort = mock(PersonLookupPort.class);
        F01LookupPort f01LookupPort = mock(F01LookupPort.class);
        AccessScopePort accessScopePort = mock(AccessScopePort.class);
        when(accessScopePort.currentScope()).thenReturn(AccessScope.ALL);
        when(accessScopePort.currentActorId()).thenReturn(Optional.of(actorId));
        service = new VoucherServiceImpl(voucherRepository, instanceRepository,
                mock(PhieuUuDaiCuaHangRepository.class), customerLookupPort, invoiceLookupPort,
                personLookupPort, f01LookupPort, accessScopePort, CLOCK);
        login(actorId, "MARKETING");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void login(UUID id, String... roles) {
        LocalIdentityFilter.LocalAuthenticationToken token = new LocalIdentityFilter.LocalAuthenticationToken(
                new com.company.aneon.shared.security.AneonPrincipal(id, "u", java.util.Set.of(roles)),
                com.company.aneon.shared.security.RolePermissions.authoritiesForRoles(java.util.Set.of(roles)));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private PhieuUuDai voucher(Integer issuedLimit, Integer perCustomerLimit) {
        PhieuUuDai voucher = new PhieuUuDai();
        voucher.setId(voucherId);
        voucher.setCode("VC100K");
        voucher.setName("Voucher 100K");
        voucher.setDiscountType(com.company.aneon.f10.constant.VoucherDiscountType.FIXED_AMOUNT);
        voucher.setValue(new BigDecimal("100000"));
        voucher.setIssuedLimit(issuedLimit);
        voucher.setIssuedCount(0);
        voucher.setUsedCount(0);
        voucher.setPerCustomerLimit(perCustomerLimit);
        voucher.setValidFrom(Instant.parse("2026-10-01T00:00:00Z"));
        voucher.setExpiresAt(Instant.parse("2026-12-31T23:59:59Z"));
        voucher.setActive(true);
        when(voucherRepository.findWithLockById(voucherId)).thenReturn(Optional.of(voucher));
        return voucher;
    }

    private PromotionDtos.IssueVoucherRequest issueRequest(String reference) {
        return new PromotionDtos.IssueVoucherRequest(customerId, reference);
    }

    @Test
    void khoVoucherHetNem409() {
        voucher(1, 0);
        when(voucherRepository.incrementIssuedCount(voucherId)).thenReturn(0);
        assertThatThrownBy(() -> service.issue(voucherId, issueRequest(null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("het");
    }

    @Test
    void unlimited0NghiaKhongGioiHan() {
        PhieuUuDai v = voucher(0, 0);
        assertThat(v.isUnlimitedIssue()).isTrue();
        when(voucherRepository.incrementIssuedCount(voucherId)).thenReturn(1);
        PromotionDtos.CustomerVoucherResponse response = service.issue(voucherId, issueRequest("DOT-1"));
        assertThat(response.status())
                .isEqualTo(com.company.aneon.f10.constant.VoucherIssueStatus.ISSUED);
        assertThat(response.duplicate()).isFalse();
    }

    @Test
    void vuotHanMucMoiKhachNem409() {
        voucher(0, 2);
        when(instanceRepository.countByVoucherIdAndCustomerIdAndStatusNot(
                voucherId, customerId, com.company.aneon.f10.constant.VoucherIssueStatus.REVOKED))
                .thenReturn(2L);
        assertThatThrownBy(() -> service.issue(voucherId, issueRequest(null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("gioi han");
    }

    @Test
    void phatLapCungIssueReferenceTraLaiPhieuDaCo() {
        voucher(0, 0);
        PhieuUuDaiKhachHang existing = new PhieuUuDaiKhachHang();
        existing.setId(instanceId);
        existing.setVoucher(voucher(0, 0));
        existing.setCustomerId(customerId);
        existing.setStatus(com.company.aneon.f10.constant.VoucherIssueStatus.ISSUED);
        existing.setIssueReference("DOT-1");
        when(instanceRepository.findByVoucherIdAndIssueReference(voucherId, "DOT-1"))
                .thenReturn(Optional.of(existing));
        PromotionDtos.CustomerVoucherResponse response = service.issue(voucherId, issueRequest("DOT-1"));
        assertThat(response.duplicate()).isTrue();
        assertThat(response.id()).isEqualTo(instanceId);
    }

    @Test
    void redeemDoiTrangThaiVaTangBoDem() {
        PhieuUuDai v = voucher(0, 0);
        PhieuUuDaiKhachHang instance = new PhieuUuDaiKhachHang();
        instance.setId(instanceId);
        instance.setVoucher(v);
        instance.setCustomerId(customerId);
        instance.setStatus(com.company.aneon.f10.constant.VoucherIssueStatus.ISSUED);
        when(instanceRepository.findWithLockById(instanceId)).thenReturn(Optional.of(instance));
        when(invoiceLookupPort.findInvoice(invoiceId)).thenReturn(Optional.of(
                new InvoiceLookupPort.InvoiceRef(invoiceId, customerId, UUID.randomUUID(),
                        new BigDecimal("500000"), true)));
        when(voucherRepository.incrementUsedCount(voucherId)).thenReturn(1);

        PromotionDtos.RedeemResult result = service.redeem(instanceId,
                new PromotionDtos.RedeemVoucherRequest(invoiceId, null));
        assertThat(result.status()).isEqualTo(com.company.aneon.f10.constant.VoucherIssueStatus.USED);
        assertThat(instance.getPurchaseInvoiceId()).isEqualTo(invoiceId);
    }

    @Test
    void redeemLaiNem409() {
        PhieuUuDai v = voucher(0, 0);
        PhieuUuDaiKhachHang instance = new PhieuUuDaiKhachHang();
        instance.setId(instanceId);
        instance.setVoucher(v);
        instance.setCustomerId(customerId);
        instance.setStatus(com.company.aneon.f10.constant.VoucherIssueStatus.USED);
        when(instanceRepository.findWithLockById(instanceId)).thenReturn(Optional.of(instance));
        assertThatThrownBy(() -> service.redeem(instanceId,
                new PromotionDtos.RedeemVoucherRequest(invoiceId, null)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.BUSINESS_CONFLICT);
    }

    @Test
    void redeemVoucherCuaNguoiKhacNem403() {
        PhieuUuDai v = voucher(0, 0);
        PhieuUuDaiKhachHang instance = new PhieuUuDaiKhachHang();
        instance.setId(instanceId);
        instance.setVoucher(v);
        instance.setCustomerId(UUID.randomUUID());
        instance.setStatus(com.company.aneon.f10.constant.VoucherIssueStatus.ISSUED);
        when(instanceRepository.findWithLockById(instanceId)).thenReturn(Optional.of(instance));
        // CUSTOMER khong lien ket voi chu voucher -> tu choi
        login(actorId, "CUSTOMER");
        assertThatThrownBy(() -> service.redeem(instanceId,
                new PromotionDtos.RedeemVoucherRequest(invoiceId, null)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    void taoVoucherPhanTramCanMucGiamToiDa() {
        assertThatThrownBy(() -> service.create(new PromotionDtos.CreateVoucherRequest(
                "PCT10", "Voucher 10%", com.company.aneon.f10.constant.VoucherDiscountType.PERCENTAGE,
                new BigDecimal("10"), null, null, 0, 0,
                Instant.parse("2026-10-01T00:00:00Z"), Instant.parse("2026-12-31T00:00:00Z"), null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("toi da");
    }

    @Test
    void taoVoucherTrungMaNem409() {
        when(voucherRepository.existsByCodeIgnoreCase("VC100K")).thenReturn(true);
        assertThatThrownBy(() -> service.create(new PromotionDtos.CreateVoucherRequest(
                "VC100K", "Voucher 100K",
                com.company.aneon.f10.constant.VoucherDiscountType.FIXED_AMOUNT,
                new BigDecimal("100000"), null, null, 0, 0,
                Instant.parse("2026-10-01T00:00:00Z"), Instant.parse("2026-12-31T00:00:00Z"), null)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.BUSINESS_CONFLICT);
    }
}
