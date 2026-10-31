package com.company.aneon.f10.service;

import java.util.UUID;

import com.company.aneon.f10.dto.PromotionDtos;

public interface VoucherService {

    /** F10UC02.2: tao kho voucher. */
    PromotionDtos.VoucherSummary create(PromotionDtos.CreateVoucherRequest request);

    /** Phat voucher cho khach (bo tro F10UC02.3), idempotent theo issueReference. */
    PromotionDtos.CustomerVoucherResponse issue(UUID voucherId, PromotionDtos.IssueVoucherRequest request);

    /** Dung voucher (ISSUED -> USED). */
    PromotionDtos.RedeemResult redeem(UUID instanceId, PromotionDtos.RedeemVoucherRequest request);
}
