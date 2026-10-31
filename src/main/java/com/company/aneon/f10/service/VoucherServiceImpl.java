package com.company.aneon.f10.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.company.aneon.f10.constant.VoucherIssueStatus;
import com.company.aneon.f10.dto.PromotionDtos;
import com.company.aneon.f10.entity.PhieuUuDai;
import com.company.aneon.f10.entity.PhieuUuDaiCuaHang;
import com.company.aneon.f10.entity.PhieuUuDaiKhachHang;
import com.company.aneon.f10.integration.CustomerLookupPort;
import com.company.aneon.f10.integration.InvoiceLookupPort;
import com.company.aneon.f10.integration.PersonLookupPort;
import com.company.aneon.f10.repository.PhieuUuDaiCuaHangRepository;
import com.company.aneon.f10.repository.PhieuUuDaiKhachHangRepository;
import com.company.aneon.f10.repository.PhieuUuDaiRepository;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.error.ErrorCode;
import com.company.aneon.shared.security.AccessScopePort;
import com.company.aneon.shared.security.RolePermissions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * F10UC02.2/02.3 bo tro phat/dung voucher. Khoa dong dong voucher +
 * UPDATE co dieu kien: hai request khong vuot tong/per-customer limit;
 * issuedLimit = 0 la khong gioi han (rule f10, D0006).
 */
@Service
public class VoucherServiceImpl implements VoucherService {

    private final PhieuUuDaiRepository voucherRepository;
    private final PhieuUuDaiKhachHangRepository instanceRepository;
    private final PhieuUuDaiCuaHangRepository voucherStoreRepository;
    private final CustomerLookupPort customerLookupPort;
    private final InvoiceLookupPort invoiceLookupPort;
    private final PersonLookupPort personLookupPort;
    private final com.company.aneon.f10.integration.F01LookupPort f01LookupPort;
    private final AccessScopePort accessScopePort;
    private final Clock clock;

    public VoucherServiceImpl(PhieuUuDaiRepository voucherRepository,
                              PhieuUuDaiKhachHangRepository instanceRepository,
                              PhieuUuDaiCuaHangRepository voucherStoreRepository,
                              CustomerLookupPort customerLookupPort, InvoiceLookupPort invoiceLookupPort,
                              PersonLookupPort personLookupPort,
                              com.company.aneon.f10.integration.F01LookupPort f01LookupPort,
                              AccessScopePort accessScopePort,
                              Clock clock) {
        this.voucherRepository = voucherRepository;
        this.instanceRepository = instanceRepository;
        this.voucherStoreRepository = voucherStoreRepository;
        this.customerLookupPort = customerLookupPort;
        this.invoiceLookupPort = invoiceLookupPort;
        this.personLookupPort = personLookupPort;
        this.f01LookupPort = f01LookupPort;
        this.accessScopePort = accessScopePort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public PromotionDtos.VoucherSummary create(PromotionDtos.CreateVoucherRequest request) {
        String code = request.code().trim();
        if (voucherRepository.existsByCodeIgnoreCase(code)) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Ma voucher da ton tai: " + code);
        }
        if (!request.validFrom().isBefore(request.expiresAt())) {
            throw ApiException.validation("expiresAt", "Hieu luc phai truoc han het");
        }
        if (request.discountType() == com.company.aneon.f10.constant.VoucherDiscountType.PERCENTAGE
                && (request.maximumDiscount() == null
                        || request.maximumDiscount().signum() <= 0)) {
            throw ApiException.validation("maximumDiscount", "Voucher phan tram can muc giam toi da");
        }
        List<UUID> storeIds = request.participatingStoreIds() == null ? List.of()
                : request.participatingStoreIds().stream().distinct().toList();
        for (UUID storeId : storeIds) {
            var store = f01LookupPort.findStore(storeId)
                    .orElseThrow(() -> ApiException.notFound("Cua hang", storeId));
            if (store.archived()) {
                throw ApiException.validation("participatingStoreIds", "Cua hang da luu tru: " + store.code());
            }
        }
        UUID actorId = requireActor();

        PhieuUuDai voucher = new PhieuUuDai();
        voucher.setId(UUID.randomUUID());
        voucher.setCode(code);
        voucher.setName(request.name().trim());
        voucher.setDiscountType(request.discountType());
        voucher.setValue(request.value());
        voucher.setMaximumDiscount(request.maximumDiscount());
        voucher.setMinimumInvoiceAmount(request.minimumInvoiceAmount());
        voucher.setIssuedLimit(request.issuedLimit());
        voucher.setIssuedCount(0);
        voucher.setUsedCount(0);
        voucher.setPerCustomerLimit(request.perCustomerLimit());
        voucher.setValidFrom(request.validFrom());
        voucher.setExpiresAt(request.expiresAt());
        voucher.setActive(true);
        voucher.setCreatedBy(actorId);
        voucherRepository.save(voucher);

        for (UUID storeId : storeIds) {
            voucherStoreRepository.save(new PhieuUuDaiCuaHang(UUID.randomUUID(), voucher.getId(), storeId));
        }
        return new PromotionDtos.VoucherSummary(voucher.getId(), voucher.getCode(), voucher.getName(),
                voucher.getDiscountType(), voucher.getValue(), voucher.getIssuedLimit(),
                voucher.getIssuedCount(), voucher.getUsedCount(), voucher.getValidFrom(),
                voucher.getExpiresAt(), voucher.isActive());
    }

    @Override
    @Transactional
    public PromotionDtos.CustomerVoucherResponse issue(UUID voucherId, PromotionDtos.IssueVoucherRequest request) {
        requirePermission(RolePermissions.F10_VOUCHER_ISSUE);
        PhieuUuDai voucher = voucherRepository.findWithLockById(voucherId)
                .orElseThrow(() -> ApiException.notFound("Voucher", voucherId));
        if (voucher.getArchivedAt() != null || !voucher.isActive()) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Voucher khong con phat hanh");
        }
        Instant now = Instant.now(clock);
        if (voucher.getExpiresAt() != null && now.isAfter(voucher.getExpiresAt())) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Voucher da het han phat hanh");
        }
        if (!customerLookupPort.customerExists(request.customerId())) {
            throw ApiException.notFound("Khach mua sam", request.customerId());
        }
        if (!voucher.isUnlimitedPerCustomer()) {
            long owned = instanceRepository.countByVoucherIdAndCustomerIdAndStatusNot(
                    voucherId, request.customerId(), VoucherIssueStatus.REVOKED);
            if (owned >= voucher.getPerCustomerLimit()) {
                throw new ApiException(ErrorCode.BUSINESS_CONFLICT,
                        "Khach da dat gioi han " + voucher.getPerCustomerLimit() + " voucher loai nay");
            }
        }
        if (request.issueReference() != null && !request.issueReference().isBlank()) {
            var existing = instanceRepository.findByVoucherIdAndIssueReference(voucherId,
                    request.issueReference().trim());
            if (existing.isPresent()) {
                return toResponse(existing.get(), true);
            }
        }
        if (voucherRepository.incrementIssuedCount(voucherId) == 0) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Kho voucher da het");
        }

        PhieuUuDaiKhachHang instance = new PhieuUuDaiKhachHang();
        instance.setId(UUID.randomUUID());
        instance.setVoucher(voucher);
        instance.setCustomerId(request.customerId());
        instance.setIssuedAt(now);
        instance.setStatus(VoucherIssueStatus.ISSUED);
        instance.setIssueReference(request.issueReference() == null ? null : request.issueReference().trim());
        instance.setCreatedBy(requireActor());
        // UNIQUE(voucher_id, issue_reference) chot canh tranh phat lap
        instanceRepository.save(instance);
        return toResponse(instance, false);
    }

    @Override
    @Transactional
    public PromotionDtos.RedeemResult redeem(UUID instanceId, PromotionDtos.RedeemVoucherRequest request) {
        PhieuUuDaiKhachHang instance = instanceRepository.findWithLockById(instanceId)
                .orElseThrow(() -> ApiException.notFound("Voucher cua khach", instanceId));
        PhieuUuDai voucher = instance.getVoucher();
        UUID actorId = requireActor();
        UUID customerId = resolveCustomerId(instance.getCustomerId(), actorId);

        if (instance.getStatus() != VoucherIssueStatus.ISSUED) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT,
                    "Voucher o trang thai " + instance.getStatus() + ", khong the su dung");
        }
        Instant now = Instant.now(clock);
        if (voucher.getValidFrom() != null && now.isBefore(voucher.getValidFrom())) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Voucher chua den hieu luc");
        }
        if (voucher.getExpiresAt() != null && now.isAfter(voucher.getExpiresAt())) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Voucher da het han");
        }
        InvoiceLookupPort.InvoiceRef invoice = invoiceLookupPort.findInvoice(request.invoiceId())
                .orElseThrow(() -> ApiException.notFound("Hoa don", request.invoiceId()));
        if (!invoice.verified()) {
            throw ApiException.validation("invoiceId", "Hoa don chua duoc xac thuc");
        }
        if (!invoice.customerId().equals(customerId)) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Hoa don khong thuoc chu voucher");
        }
        if (voucher.getMinimumInvoiceAmount() != null
                && invoice.totalAmount().compareTo(voucher.getMinimumInvoiceAmount()) < 0) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Hoa don khong du gia tri toi thieu");
        }
        List<UUID> allowedStores = voucherStoreRepository.findByVoucherId(voucher.getId()).stream()
                .map(PhieuUuDaiCuaHang::getStoreId).toList();
        if (!allowedStores.isEmpty() && !allowedStores.contains(invoice.storeId())) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Voucher khong ap dung tai cua hang nay");
        }

        instance.setStatus(VoucherIssueStatus.USED);
        instance.setRedeemedAt(now);
        instance.setPurchaseInvoiceId(invoice.id());
        instanceRepository.save(instance);
        if (voucherRepository.incrementUsedCount(voucher.getId()) == 0) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Khong cap nhat duoc bo dem voucher");
        }
        return new PromotionDtos.RedeemResult(instance.getId(), instance.getStatus(),
                instance.getRedeemedAt(), invoice.id());
    }

    /** Khach tu dung voucher cua minh; nhan vien can quyen assist. */
    private UUID resolveCustomerId(UUID instanceCustomerId, UUID actorId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAssistant = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> new SimpleGrantedAuthority(RolePermissions.F10_CHECKIN_ASSIST).equals(a));
        if (isAssistant) {
            return instanceCustomerId;
        }
        if (personLookupPort.customerLinkOf(actorId)
                .map(linked -> linked.equals(instanceCustomerId)).orElse(false)) {
            return instanceCustomerId;
        }
        throw ApiException.forbidden("Khong co quyen dung voucher cua nguoi khac");
    }

    private PromotionDtos.CustomerVoucherResponse toResponse(PhieuUuDaiKhachHang instance, boolean duplicate) {
        return new PromotionDtos.CustomerVoucherResponse(instance.getId(), instance.getVoucher().getId(),
                instance.getVoucher().getCode(), instance.getCustomerId(), instance.getStatus(),
                instance.getIssuedAt(), instance.getIssueReference(), duplicate);
    }

    private void requirePermission(String permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().stream()
                .noneMatch(a -> new SimpleGrantedAuthority(permission).equals(a))) {
            throw ApiException.forbidden("Khong co quyen thuc hien thao tac nay");
        }
    }

    private UUID requireActor() {
        return accessScopePort.currentActorId().orElseThrow(
                () -> new ApiException(ErrorCode.UNAUTHENTICATED, "Khong xac dinh duoc nguoi thao tac"));
    }
}
