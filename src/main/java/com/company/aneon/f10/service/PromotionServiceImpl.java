package com.company.aneon.f10.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.company.aneon.f10.constant.PromotionStatus;
import com.company.aneon.f10.constant.PromotionType;
import com.company.aneon.f10.constant.VoucherDiscountType;
import com.company.aneon.f10.constant.VoucherIssueStatus;
import com.company.aneon.f10.dto.PromotionDtos;
import com.company.aneon.f10.entity.ChuongTrinhGiamGia;
import com.company.aneon.f10.entity.ChuongTrinhKhuyenMai;
import com.company.aneon.f10.entity.ChuongTrinhTangPhieuUuDai;
import com.company.aneon.f10.entity.ChuongTrinhTichDiem;
import com.company.aneon.f10.entity.KhuyenMaiCuaHang;
import com.company.aneon.f10.entity.LanApDungKhuyenMai;
import com.company.aneon.f10.entity.PhieuUuDai;
import com.company.aneon.f10.entity.PhieuUuDaiCuaHang;
import com.company.aneon.f10.entity.PhieuUuDaiKhachHang;
import com.company.aneon.f10.integration.CustomerLookupPort;
import com.company.aneon.f10.integration.F01LookupPort;
import com.company.aneon.f10.integration.InvoiceLookupPort;
import com.company.aneon.f10.repository.ChuongTrinhKhuyenMaiRepository;
import com.company.aneon.f10.repository.KhuyenMaiCuaHangRepository;
import com.company.aneon.f10.repository.LanApDungKhuyenMaiRepository;
import com.company.aneon.f10.repository.PhieuUuDaiKhachHangRepository;
import com.company.aneon.f10.repository.PhieuUuDaiRepository;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.error.ErrorCode;
import com.company.aneon.shared.security.AccessScopePort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * F10UC02.1/02.3 + ap dung khuyen mai bo tro. Dung mot loai duy nhat;
 * MySQL la nguon bo dem (D0006); idempotency key UNIQUE (V2).
 */
@Service
public class PromotionServiceImpl implements PromotionService {

    private final ChuongTrinhKhuyenMaiRepository promotionRepository;
    private final PhieuUuDaiRepository voucherRepository;
    private final PhieuUuDaiKhachHangRepository customerVoucherRepository;
    private final LanApDungKhuyenMaiRepository applicationRepository;
    private final KhuyenMaiCuaHangRepository promotionStoreRepository;
    private final F01LookupPort f01LookupPort;
    private final CustomerLookupPort customerLookupPort;
    private final InvoiceLookupPort invoiceLookupPort;
    private final AccessScopePort accessScopePort;
    private final Clock clock;

    public PromotionServiceImpl(ChuongTrinhKhuyenMaiRepository promotionRepository,
                                PhieuUuDaiRepository voucherRepository,
                                PhieuUuDaiKhachHangRepository customerVoucherRepository,
                                LanApDungKhuyenMaiRepository applicationRepository,
                                KhuyenMaiCuaHangRepository promotionStoreRepository,
                                F01LookupPort f01LookupPort, CustomerLookupPort customerLookupPort,
                                InvoiceLookupPort invoiceLookupPort, AccessScopePort accessScopePort,
                                Clock clock) {
        this.promotionRepository = promotionRepository;
        this.voucherRepository = voucherRepository;
        this.customerVoucherRepository = customerVoucherRepository;
        this.applicationRepository = applicationRepository;
        this.promotionStoreRepository = promotionStoreRepository;
        this.f01LookupPort = f01LookupPort;
        this.customerLookupPort = customerLookupPort;
        this.invoiceLookupPort = invoiceLookupPort;
        this.accessScopePort = accessScopePort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public PromotionDtos.PromotionSummary create(PromotionDtos.CreatePromotionRequest request) {
        String code = request.code().trim();
        if (promotionRepository.existsByCodeIgnoreCase(code)) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Ma chuong trinh da ton tai: " + code);
        }
        if (!request.startAt().isBefore(request.endAt())) {
            throw ApiException.validation("endAt", "Thoi gian ket thuc phai sau thoi gian bat dau");
        }
        List<UUID> storeIds = request.participatingStoreIds() == null ? List.of()
                : request.participatingStoreIds().stream().distinct().toList();
        for (UUID storeId : storeIds) {
            F01LookupPort.StoreRef store = f01LookupPort.findStore(storeId)
                    .orElseThrow(() -> ApiException.notFound("Cua hang", storeId));
            if (store.archived()) {
                throw ApiException.validation("participatingStoreIds", "Cua hang da luu tru: " + store.code());
            }
        }

        ChuongTrinhKhuyenMai program;
        switch (request.type()) {
            case DISCOUNT -> {
                requireNull(request.giveaway(), "giveaway");
                requireNull(request.points(), "points");
                if (request.discount() == null) {
                    throw ApiException.validation("discount", "Loai DISCOUNT can cau hinh giam gia");
                }
                ChuongTrinhGiamGia discount = new ChuongTrinhGiamGia();
                discount.setDiscountRate(request.discount().discountRatePercent());
                discount.setMaximumDiscount(request.discount().maximumDiscount());
                program = discount;
            }
            case VOUCHER_GIVEAWAY -> {
                requireNull(request.discount(), "discount");
                requireNull(request.points(), "points");
                if (request.giveaway() == null) {
                    throw ApiException.validation("giveaway", "Loai VOUCHER_GIVEAWAY can cau hinh voucher");
                }
                PhieuUuDai voucher = voucherRepository.findById(request.giveaway().voucherId())
                        .filter(v -> v.getArchivedAt() == null)
                        .orElseThrow(() -> ApiException.notFound("Voucher", request.giveaway().voucherId()));
                ChuongTrinhTangPhieuUuDai giveaway = new ChuongTrinhTangPhieuUuDai();
                giveaway.setVoucher(voucher);
                giveaway.setTotalToIssue(request.giveaway().totalToIssue());
                giveaway.setIssuedCount(0);
                program = giveaway;
            }
            case POINT_ACCUMULATION -> {
                requireNull(request.discount(), "discount");
                requireNull(request.giveaway(), "giveaway");
                if (request.points() == null) {
                    throw ApiException.validation("points", "Loai POINT_ACCUMULATION can cau hinh diem");
                }
                if (request.points().conversionAmount() == null
                        || request.points().conversionAmount().signum() <= 0
                        || request.points().pointsPerConversion() == null
                        || request.points().pointsPerConversion() <= 0) {
                    throw ApiException.validation("points",
                            "Can conversionAmount > 0 va pointsPerConversion > 0");
                }
                ChuongTrinhTichDiem points = new ChuongTrinhTichDiem();
                points.setRewardPoints(request.points().rewardPoints());
                points.setConversionAmount(request.points().conversionAmount());
                points.setPointsPerConversion(request.points().pointsPerConversion());
                program = points;
            }
            default -> throw ApiException.validation("type", "Loai khuyen mai khong hop le");
        }

        program.setId(UUID.randomUUID());
        program.setCode(code);
        program.setName(request.name().trim());
        program.setType(request.type());
        program.setStartAt(request.startAt());
        program.setEndAt(request.endAt());
        program.setMinimumInvoiceAmount(request.minimumInvoiceAmount());
        program.setTargetTiers(request.targetTiers());
        program.setTerms(request.terms());
        program.setPublicVisible(Boolean.TRUE.equals(request.publicVisible()));
        program.setStatus(initialStatus(request.startAt(), request.endAt()));
        program.setCreatedBy(requireActor());
        promotionRepository.save(program);

        for (UUID storeId : storeIds) {
            promotionStoreRepository.save(new KhuyenMaiCuaHang(UUID.randomUUID(), program.getId(), storeId));
        }
        return new PromotionDtos.PromotionSummary(program.getId(), program.getCode(), program.getName(),
                program.getType(), program.getStatus(), program.getStartAt(), program.getEndAt(),
                program.isPublicVisible());
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionDtos.PromotionRealtimeResponse realtime(UUID promotionId) {
        ChuongTrinhKhuyenMai program = promotionRepository.findAnyById(promotionId)
                .filter(p -> p.getArchivedAt() == null)
                .orElseThrow(() -> ApiException.notFound("Chuong trinh", promotionId));
        var aggregate = applicationRepository.aggregateByPromotion(promotionId);

        Integer totalLimit = null;
        Integer issuedCount = null;
        Integer remaining = null;
        if (program instanceof ChuongTrinhTangPhieuUuDai giveaway) {
            totalLimit = giveaway.getTotalToIssue();
            PhieuUuDai voucher = giveaway.getVoucher();
            issuedCount = voucher == null ? 0 : voucher.getIssuedCount();
            remaining = totalLimit == null || totalLimit == 0 ? null
                    : Math.max(totalLimit - issuedCount, 0);
        }
        return new PromotionDtos.PromotionRealtimeResponse(promotionId, program.getType(),
                totalLimit, issuedCount, remaining, (int) aggregate.getApplyCount(),
                aggregate.getTotalDiscount(), aggregate.getTotalPoints(),
                Instant.now(clock));
    }

    @Override
    @Transactional
    public PromotionDtos.ApplyPromotionResult apply(UUID promotionId, PromotionDtos.ApplyPromotionRequest request) {
        ChuongTrinhKhuyenMai program = promotionRepository.findAnyById(promotionId)
                .filter(p -> p.getArchivedAt() == null)
                .orElseThrow(() -> ApiException.notFound("Chuong trinh", promotionId));
        Instant now = Instant.now(clock);
        if ((program.getStatus() != PromotionStatus.ACTIVE && program.getStatus() != PromotionStatus.SCHEDULED)
                || program.getStartAt() == null || now.isBefore(program.getStartAt())
                || program.getEndAt() == null || now.isAfter(program.getEndAt())) {
            throw ApiException.conflict("Chuong trinh khong trong thoi gian ap dung");
        }

        // Idempotency: cung key tra lai ket qua lan ap dung dau
        var existing = applicationRepository.findByIdempotencyKey(request.idempotencyKey().trim());
        if (existing.isPresent()) {
            LanApDungKhuyenMai prior = existing.get();
            if (!prior.getPromotionId().equals(promotionId)) {
                throw ApiException.conflict("Idempotency key da dung cho chuong trinh khac");
            }
            return new PromotionDtos.ApplyPromotionResult(prior.getId(), prior.getDiscountAmount(),
                    prior.getPointsGranted(), true);
        }

        if (!customerLookupPort.customerExists(request.customerId())) {
            throw ApiException.notFound("Khach mua sam", request.customerId());
        }
        InvoiceLookupPort.InvoiceRef invoice = invoiceLookupPort.findInvoice(request.invoiceId())
                .orElseThrow(() -> ApiException.notFound("Hoa don", request.invoiceId()));
        if (!invoice.verified()) {
            throw ApiException.validation("invoiceId", "Hoa don chua duoc xac thuc tu nguon ban hang");
        }
        if (!invoice.customerId().equals(request.customerId())) {
            throw ApiException.conflict("Hoa don khong thuoc khach hang nay");
        }
        BigDecimal minimum = program.getMinimumInvoiceAmount();
        if (minimum != null && invoice.totalAmount().compareTo(minimum) < 0) {
            throw ApiException.conflict("Hoa don khong du gia tri toi thieu "
                    + minimum + " de ap dung khuyen mai");
        }

        BigDecimal discountAmount = null;
        Long pointsGranted = null;
        UUID customerVoucherId = null;
        switch (program.getType()) {
            case DISCOUNT -> {
                ChuongTrinhGiamGia discount = (ChuongTrinhGiamGia) program;
                // discountRate theo thang 0..100; lam tron HALF_UP 2 chu so thap phan (D0009)
                discountAmount = invoice.totalAmount()
                        .multiply(discount.getDiscountRate())
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                if (discount.getMaximumDiscount() != null
                        && discountAmount.compareTo(discount.getMaximumDiscount()) > 0) {
                    discountAmount = discount.getMaximumDiscount();
                }
            }
            case VOUCHER_GIVEAWAY -> {
                if (request.customerVoucherId() == null) {
                    throw ApiException.validation("customerVoucherId",
                            "Ap dung tang voucher can voucher cua khach hang");
                }
                PhieuUuDaiKhachHang instance = customerVoucherRepository
                        .findWithLockById(request.customerVoucherId())
                        .orElseThrow(() -> ApiException.notFound("Voucher cua khach", request.customerVoucherId()));
                if (!instance.getCustomerId().equals(request.customerId())
                        || instance.getStatus() != VoucherIssueStatus.ISSUED) {
                    throw ApiException.conflict("Voucher khong dung chu hoac da su dung");
                }
                PhieuUuDai voucher = instance.getVoucher();
                discountAmount = voucherDiscount(voucher, invoice.totalAmount());
                instance.setStatus(VoucherIssueStatus.USED);
                instance.setRedeemedAt(now);
                instance.setPurchaseInvoiceId(invoice.id());
                customerVoucherRepository.save(instance);
                if (voucherRepository.incrementUsedCount(voucher.getId()) == 0) {
                    throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Khong cap nhat duoc bo dem voucher");
                }
                customerVoucherId = instance.getId();
            }
            case POINT_ACCUMULATION -> {
                ChuongTrinhTichDiem points = (ChuongTrinhTichDiem) program;
                pointsGranted = points.getRewardPoints() == null ? 0 : points.getRewardPoints();
            }
        }

        LanApDungKhuyenMai application = new LanApDungKhuyenMai();
        application.setId(UUID.randomUUID());
        application.setPromotionId(promotionId);
        application.setCustomerId(request.customerId());
        application.setInvoiceId(invoice.id());
        application.setAppliedAt(now);
        application.setDiscountAmount(discountAmount);
        application.setPointsGranted(pointsGranted);
        application.setCustomerVoucherId(customerVoucherId);
        application.setIdempotencyKey(request.idempotencyKey().trim());
        application.setCreatedBy(requireActor());
        application.setCreatedAt(now);
        // UNIQUE(idempotency_key) chot canh tranh: request thua nhan 409 qua handler
        applicationRepository.save(application);
        return new PromotionDtos.ApplyPromotionResult(application.getId(), discountAmount,
                pointsGranted, false);
    }

    private BigDecimal voucherDiscount(PhieuUuDai voucher, BigDecimal invoiceTotal) {
        if (voucher.getMinimumInvoiceAmount() != null
                && invoiceTotal.compareTo(voucher.getMinimumInvoiceAmount()) < 0) {
            throw ApiException.conflict("Hoa don khong du gia tri toi thieu cua voucher");
        }
        if (voucher.getDiscountType() == VoucherDiscountType.FIXED_AMOUNT) {
            BigDecimal value = voucher.getValue();
            return value.min(invoiceTotal);
        }
        // PERCENTAGE theo thang 0..100, cap theo maximumDiscount (neu co)
        BigDecimal amount = invoiceTotal.multiply(voucher.getValue())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return voucher.getMaximumDiscount() == null ? amount
                : amount.min(voucher.getMaximumDiscount());
    }

    private PromotionStatus initialStatus(Instant startAt, Instant endAt) {
        Instant now = Instant.now(clock);
        if (now.isBefore(startAt)) {
            return PromotionStatus.SCHEDULED;
        }
        if (now.isAfter(endAt)) {
            return PromotionStatus.FINISHED;
        }
        return PromotionStatus.ACTIVE;
    }

    private void requireNull(Object value, String field) {
        if (value != null) {
            throw ApiException.validation(field, "Khong duoc truong khi chon loai khuyen mai khac");
        }
    }

    private UUID requireActor() {
        return accessScopePort.currentActorId().orElseThrow(
                () -> new ApiException(ErrorCode.UNAUTHENTICATED, "Khong xac dinh duoc nguoi thao tac"));
    }
}
