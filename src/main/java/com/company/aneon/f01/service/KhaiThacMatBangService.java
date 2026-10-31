package com.company.aneon.f01.service;

import java.util.UUID;

import com.company.aneon.f01.dto.UtilizationDtos;
import com.company.aneon.f01.constant.PremiseStatus;

public interface KhaiThacMatBangService {

    /**
     * F01UC05.1 - 05.4: tong quan khai thac + loc theo khu vuc/tang/trang thai.
     */
    UtilizationDtos.UtilizationPage getUtilization(UUID zoneId, UUID floorId,
                                                   PremiseStatus status, int page, int size);
}
