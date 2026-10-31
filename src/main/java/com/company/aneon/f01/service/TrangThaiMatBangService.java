package com.company.aneon.f01.service;

import com.company.aneon.f01.constant.PremiseStatus;
import com.company.aneon.f01.dto.StatusDtos;
import com.company.aneon.shared.web.PageResponse;

import java.util.UUID;

public interface TrangThaiMatBangService {

    PageResponse<StatusDtos.StatusItem> getCurrentStatuses(PremiseStatus status, int page, int size);

    StatusDtos.StatusChangeResult updatePremiseStatus(UUID id, StatusDtos.ChangeStatusRequest request);

    StatusDtos.SyncStatusResult syncStatusFromContract(UUID id, StatusDtos.SyncContractStatusRequest request);
}
