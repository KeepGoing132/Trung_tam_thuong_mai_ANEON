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

import com.company.aneon.f10.constant.PromotionStatus;
import com.company.aneon.f10.constant.PromotionType;
import com.company.aneon.f10.constant.VoucherDiscountType;
import com.company.aneon.f10.constant.VoucherIssueStatus;
import com.company.aneon.f10.dto.PromotionDtos;
import com.company.aneon.f10.entity.ChuongTrinhGiamGia;
import com.company.aneon.f10.entity.LanApDungKhuyenMai;
import com.company.aneon.f10.entity.PhieuUuDai;
import com.company.aneon.f10.entity.PhieuUuDaiKhachHang;
import com.company.aneon.f10.integration.CustomerLookupPort;
import com.company.aneon.f10.integration.F01LookupPort;
import com.company.aneon.f10.integration.InvoiceLookupPort;
import com.company.aneon.f10.repository.ChuongTrinhKhuyenMaiRepository;
import com.company.aneon.f10.repository.KhuyenMaiCuaHangRepository;
import com.company.aneon.f10.repository.LanApDungKhuyenMaiRepository;
import com.company.aneon.f10.repository.PhieuUuDaiKhachHangRepository;
import com.company.aneon.f10.repository.PhieuUuDaiRepository;
import com.company.aneon.f10.service.PromotionServiceImpl;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.error.ErrorCode;
import com.company.aneon.shared.security.AccessScope;
import com.company.aneon.shared.security.AccessScopePort;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * F10UC02.1/02.3: mot loai duy nhat; ty le 0..100; idempotency apply;
 * invoice xac minh tu nguon. (Test voi login neu goi apply can actor.)
 */
class PromotionServiceImplTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-01T00:00:00Z"), ZoneOffset.UTC);

    private ChuongTrinhKhuyenMaiRepository promotionRepository;
    private LanApDungKhuyenMaiRepository applicationRepository;
    private PhieuUuDaiRepository voucherRepository;
    private PhieuUuDaiKhachHangRepository instanceRepository;
    private InvoiceLookupPort invoiceLookupPort;
    private PromotionServiceImpl service;

    private final UUID actorId = UUID.randomUUID();
    private final UUID promotionId = UUID.randomUUID();
    private final UUID invoiceId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final Instant start = Instant.parse("2026-10-01T00:00:00Z");
    private final Instant end = Instant.parse("2026-12-31T00:00:00Z");

    @BeforeEach
    void setUp() {
        promotionRepository = mock(ChuongTrinhKhuyenMaiRepository.class);
        applicationRepository = mock(LanApDungKhuyenMaiRepository.class);
        voucherRepository = mock(PhieuUuDaiRepository.class);
        instanceRepository = mock(PhieuUuDaiKhachHangRepository.class);
        invoiceLookupPort = mock(InvoiceLookupPort.class);
        CustomerLookupPort customerLookupPort = mock(CustomerLookupPort.class);
        when(customerLookupPort.customerExists(any())).thenReturn(true);
        AccessScopePort accessScopePort = mock(AccessScopePort.class);
        when(accessScopePort.currentScope()).thenReturn(AccessScope.ALL);
        when(accessScopePort.currentActorId()).thenReturn(Optional.of(actorId));
        service = new PromotionServiceImpl(promotionRepository, voucherRepository, instanceRepository,
                applicationRepository, mock(KhuyenMaiCuaHangRepository.class),
                mock(F01LookupPort.class), customerLookupPort, invoiceLookupPort,
                accessScopePort, CLOCK);
        SecurityContextHolder.getContext().setAuthentication(
                new com.company.aneon.shared.security.LocalIdentityFilter.LocalAuthenticationToken(
                        new com.company.aneon.shared.security.AneonPrincipal(actorId, "u",
                                java.util.Set.of("MARKETING")),
                        com.company.aneon.shared.security.RolePermissions.authoritiesForRoles(
                                java.util.Set.of("MARKETING"))));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private ChuongTrinhGiamGia discountPromotion(BigDecimal rate, BigDecimal maxDiscount) {
        ChuongTrinhGiamGia promotion = new ChuongTrinhGiamGia();
        promotion.setId(promotionId);
        promotion.setCode("KM01");
        promotion.setName("Khuyen mai giam gia");
        promotion.setType(PromotionType.DISCOUNT);
        promotion.setStartAt(start);
        promotion.setEndAt(end);
        promotion.setStatus(PromotionStatus.ACTIVE);
        promotion.setDiscountRate(rate);
        promotion.setMaximumDiscount(maxDiscount);
        when(promotionRepository.findAnyById(promotionId)).thenReturn(Optional.of(promotion));
        return promotion;
    }

    private PromotionDtos.CreatePromotionRequest request(PromotionType type,
                                                         PromotionDtos.DiscountSpec discount,
                                                         PromotionDtos.GiveawaySpec giveaway,
                                                         PromotionDtos.PointsSpec points) {
        return new PromotionDtos.CreatePromotionRequest("KM01", "KM 01", type, start, end,
                null, null, null, false, null, discount, giveaway, points);
    }

    @Test
    void loaiDISCOUNTKhongNhanTruongKhac() {
        assertThatThrownBy(() -> service.create(request(PromotionType.DISCOUNT,
                new PromotionDtos.DiscountSpec(new BigDecimal("10"), null),
                new PromotionDtos.GiveawaySpec(UUID.randomUUID(), 10), null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Khong duoc truong");
    }

    @Test
    void loaiDISCOUNTBatBuocCoCauHinh() {
        assertThatThrownBy(() -> service.create(request(PromotionType.DISCOUNT, null, null, null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("DISCOUNT");
    }

    @Test
    void loaiVOUCHER_GIVEAWAYBatBuocVoucherTonTai() {
        when(voucherRepository.findById(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(request(PromotionType.VOUCHER_GIVEAWAY, null,
                new PromotionDtos.GiveawaySpec(UUID.randomUUID(), 100), null)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    void loaiTichDiemBatBuocQuyDoiDuong() {
        assertThatThrownBy(() -> service.create(request(PromotionType.POINT_ACCUMULATION, null, null,
                new PromotionDtos.PointsSpec(10L, null, null))))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("conversionAmount");
    }

    @Test
    void trangThaiBanDauTheoThoiGian() {
        PromotionDtos.PromotionSummary summary = service.create(request(PromotionType.DISCOUNT,
                new PromotionDtos.DiscountSpec(new BigDecimal("15"), null), null, null));
        // CLOCK 2026-11-01 nam trong [10-01, 12-31] -> ACTIVE
        assertThat(summary.status()).isEqualTo(PromotionStatus.ACTIVE);
    }

    @Test
    void applyGiamGiaLamTronVaCapToiDa() {
        discountPromotion(new BigDecimal("15"), new BigDecimal("50000"));
        when(invoiceLookupPort.findInvoice(invoiceId)).thenReturn(Optional.of(
                new InvoiceLookupPort.InvoiceRef(invoiceId, customerId, UUID.randomUUID(),
                        new BigDecimal("500000"), true)));
        PromotionDtos.ApplyPromotionResult result = service.apply(promotionId,
                new PromotionDtos.ApplyPromotionRequest(invoiceId, customerId, null, "KEY-1"));
        // 500000 * 15% = 75000 <= maximum 50000? khong: 75000 > 50000 -> cap 50000
        assertThat(result.discountAmount()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(result.duplicate()).isFalse();
    }

    @Test
    void applyKhongCapKhiDuoiMucToiDa() {
        discountPromotion(new BigDecimal("15"), new BigDecimal("100000"));
        when(invoiceLookupPort.findInvoice(invoiceId)).thenReturn(Optional.of(
                new InvoiceLookupPort.InvoiceRef(invoiceId, customerId, UUID.randomUUID(),
                        new BigDecimal("333333"), true)));
        PromotionDtos.ApplyPromotionResult result = service.apply(promotionId,
                new PromotionDtos.ApplyPromotionRequest(invoiceId, customerId, null, "KEY-2"));
        // 333333 * 15% = 49999.95 -> lam tron HALF_UP 2 chu so
        assertThat(result.discountAmount()).isEqualByComparingTo(new BigDecimal("49999.95"));
    }

    @Test
    void applyIdempotencyTraLaiKetQuaLanDau() {
        discountPromotion(new BigDecimal("15"), null);
        LanApDungKhuyenMai prior = new LanApDungKhuyenMai();
        prior.setId(UUID.randomUUID());
        prior.setPromotionId(promotionId);
        prior.setDiscountAmount(new BigDecimal("75000"));
        when(applicationRepository.findByIdempotencyKey("KEY-3")).thenReturn(Optional.of(prior));
        PromotionDtos.ApplyPromotionResult result = service.apply(promotionId,
                new PromotionDtos.ApplyPromotionRequest(invoiceId, customerId, null, "KEY-3"));
        assertThat(result.duplicate()).isTrue();
        assertThat(result.discountAmount()).isEqualByComparingTo(new BigDecimal("75000"));
    }

    @Test
    void applyHoaDonChuaXacThucNem400() {
        discountPromotion(new BigDecimal("15"), null);
        when(invoiceLookupPort.findInvoice(invoiceId)).thenReturn(Optional.of(
                new InvoiceLookupPort.InvoiceRef(invoiceId, customerId, UUID.randomUUID(),
                        new BigDecimal("500000"), false)));
        assertThatThrownBy(() -> service.apply(promotionId,
                new PromotionDtos.ApplyPromotionRequest(invoiceId, customerId, null, "KEY-5")))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void applyHoaDonKhongDuGiaTriToiThieuNem409() {
        ChuongTrinhGiamGia promotion = discountPromotion(new BigDecimal("15"), null);
        promotion.setMinimumInvoiceAmount(new BigDecimal("1000000"));
        when(invoiceLookupPort.findInvoice(invoiceId)).thenReturn(Optional.of(
                new InvoiceLookupPort.InvoiceRef(invoiceId, customerId, UUID.randomUUID(),
                        new BigDecimal("500000"), true)));
        assertThatThrownBy(() -> service.apply(promotionId,
                new PromotionDtos.ApplyPromotionRequest(invoiceId, customerId, null, "KEY-6")))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.BUSINESS_CONFLICT);
    }

    @Test
    void applyVOUCHER_GIVEAWAYDungVoucherKhach() {
        // Chuong trinh tang voucher phai la subtype tang phieu
        com.company.aneon.f10.entity.ChuongTrinhTangPhieuUuDai giveaway =
                new com.company.aneon.f10.entity.ChuongTrinhTangPhieuUuDai();
        giveaway.setId(promotionId);
        giveaway.setCode("KM02");
        giveaway.setName("Tang voucher");
        giveaway.setType(PromotionType.VOUCHER_GIVEAWAY);
        giveaway.setStartAt(start);
        giveaway.setEndAt(end);
        giveaway.setStatus(PromotionStatus.ACTIVE);
        when(promotionRepository.findAnyById(promotionId)).thenReturn(Optional.of(giveaway));

        PhieuUuDai voucher = new PhieuUuDai();
        voucher.setId(UUID.randomUUID());
        voucher.setCode("VC100");
        voucher.setDiscountType(VoucherDiscountType.FIXED_AMOUNT);
        voucher.setValue(new BigDecimal("100000"));
        PhieuUuDaiKhachHang instance = new PhieuUuDaiKhachHang();
        instance.setId(UUID.randomUUID());
        instance.setVoucher(voucher);
        instance.setCustomerId(customerId);
        instance.setStatus(VoucherIssueStatus.ISSUED);
        when(instanceRepository.findWithLockById(instance.getId())).thenReturn(Optional.of(instance));
        when(invoiceLookupPort.findInvoice(invoiceId)).thenReturn(Optional.of(
                new InvoiceLookupPort.InvoiceRef(invoiceId, customerId, UUID.randomUUID(),
                        new BigDecimal("500000"), true)));
        when(voucherRepository.incrementUsedCount(voucher.getId())).thenReturn(1);

        PromotionDtos.ApplyPromotionResult result = service.apply(promotionId,
                new PromotionDtos.ApplyPromotionRequest(invoiceId, customerId, instance.getId(), "KEY-7"));
        assertThat(result.discountAmount()).isEqualByComparingTo(new BigDecimal("100000"));
        assertThat(instance.getStatus()).isEqualTo(VoucherIssueStatus.USED);
        assertThat(instance.getPurchaseInvoiceId()).isEqualTo(invoiceId);
    }
}
