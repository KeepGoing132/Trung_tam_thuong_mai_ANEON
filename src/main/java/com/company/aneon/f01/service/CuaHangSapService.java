package com.company.aneon.f01.service;

import java.util.UUID;

import com.company.aneon.f01.dto.StoreDtos;
import com.company.aneon.f01.constant.StoreStatus;
import com.company.aneon.shared.web.PageResponse;

public interface CuaHangSapService {

    PageResponse<StoreDtos.StoreListItem> list(UUID premiseId, StoreStatus status, String q, int page, int size);

    StoreDtos.StoreDetail detail(UUID id);

    StoreDtos.StoreListItem create(StoreDtos.CreateStoreRequest request);

    StoreDtos.StoreDetail update(UUID id, StoreDtos.UpdateStoreRequest request);

    StoreDtos.StoreDetail changeStatus(UUID id, StoreDtos.ChangeStoreStatusRequest request);
}
