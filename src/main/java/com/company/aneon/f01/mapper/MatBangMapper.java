package com.company.aneon.f01.mapper;

import java.math.BigDecimal;

import com.company.aneon.f01.dto.PremiseDtos;
import com.company.aneon.f01.entity.MatBang;

import org.springframework.stereotype.Component;

/**
 * Mapper chi chuyen du lieu, khong truy van DB hay doi trang thai (rule 01).
 */
@Component
public class MatBangMapper {

    public record Snapshot(String code, String name, java.util.UUID zoneId, BigDecimal totalArea,
                           String type, String status, java.util.UUID locationId, String description) {
    }

    public PremiseDtos.PremiseListItem toListItem(MatBang premise) {
        return new PremiseDtos.PremiseListItem(
                premise.getId(), premise.getCode(), premise.getName(),
                premise.getZone().getId(), premise.getZone().getName(),
                premise.getZone().getFloor().getName(),
                premise.getTotalArea(), premise.getType(), premise.getStatus());
    }

    public PremiseDtos.PremiseDetail toDetail(MatBang premise, BigDecimal allocatedArea) {
        return new PremiseDtos.PremiseDetail(
                premise.getId(), premise.getCode(), premise.getName(),
                premise.getZone().getId(), premise.getZone().getName(),
                premise.getZone().getFloor().getName(),
                premise.getZone().getFloor().getBuilding().getName(),
                premise.getTotalArea(), allocatedArea, premise.getType(), premise.getStatus(),
                premise.getLocation() == null ? null : premise.getLocation().getId(),
                premise.getDescription(), premise.getVersion());
    }

    public Snapshot toSnapshot(MatBang premise) {
        return new Snapshot(premise.getCode(), premise.getName(), premise.getZone().getId(),
                premise.getTotalArea(),
                premise.getType() == null ? null : premise.getType().name(),
                premise.getStatus().name(),
                premise.getLocation() == null ? null : premise.getLocation().getId(),
                premise.getDescription());
    }
}
