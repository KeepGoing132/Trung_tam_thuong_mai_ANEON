package com.company.aneon.f01.service;

import java.util.UUID;

import com.company.aneon.f01.constant.PremiseStatus;
import com.company.aneon.f01.dto.PremiseDtos;
import com.company.aneon.shared.web.PageResponse;

public interface MatBangService {

    PageResponse<PremiseDtos.PremiseListItem> list(String q, UUID zoneId, PremiseStatus status, int page, int size);

    PremiseDtos.PremiseDetail detail(UUID id);

    PremiseDtos.CreatePremiseResult create(PremiseDtos.CreatePremiseRequest request);

    PremiseDtos.UpdateResult update(UUID id, PremiseDtos.UpdatePremiseRequest request);

    PremiseDtos.CodeAvailability checkCodeAvailable(String code, UUID excludeId);

    PremiseDtos.LocationUpdateResult relocate(UUID id, PremiseDtos.UpdatePremiseLocationRequest request);
}
