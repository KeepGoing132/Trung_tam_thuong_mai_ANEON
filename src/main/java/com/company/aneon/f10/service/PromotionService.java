package com.company.aneon.f10.service;

import java.util.UUID;

import com.company.aneon.f10.dto.PromotionDtos;

public interface PromotionService {

    /** F10UC02.1: tao chuong trinh, dung mot loai duy nhat. */
    PromotionDtos.PromotionSummary create(PromotionDtos.CreatePromotionRequest request);

    /** F10UC02.3: so lieu tu MySQL (D0006). */
    PromotionDtos.PromotionRealtimeResponse realtime(UUID promotionId);

    /** Ap dung khuyen mai cho hoa don (bo tro F10UC02.3), idempotent theo key. */
    PromotionDtos.ApplyPromotionResult apply(UUID promotionId, PromotionDtos.ApplyPromotionRequest request);
}
