package com.company.aneon.f01.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.company.aneon.f01.dto.UtilizationDtos;
import com.company.aneon.f01.entity.MatBang;
import com.company.aneon.f01.repository.CuaHangSapRepository;

import org.springframework.stereotype.Component;

/**
 * Mapper khai thac: gom dien tich phan bo cho nhieu mat bang trong mot
 * truy van group by (khong N+1).
 */
@Component
public class KhaiThacMapper {

    private final CuaHangSapRepository cuaHangSapRepository;

    public KhaiThacMapper(CuaHangSapRepository cuaHangSapRepository) {
        this.cuaHangSapRepository = cuaHangSapRepository;
    }

    public List<UtilizationDtos.UtilizationItem> toItems(List<MatBang> premises) {
        if (premises.isEmpty()) {
            return List.of();
        }
        Map<UUID, BigDecimal> allocated = cuaHangSapRepository
                .sumAllocatedAreas(premises.stream().map(MatBang::getId).toList())
                .stream()
                .collect(Collectors.toMap(row -> (UUID) row[0], row -> (BigDecimal) row[1]));
        return premises.stream().map(p -> new UtilizationDtos.UtilizationItem(
                p.getId(), p.getCode(), p.getName(),
                p.getZone().getName(), p.getZone().getFloor().getName(),
                p.getZone().getFloor().getBuilding().getName(),
                p.getTotalArea(),
                allocated.getOrDefault(p.getId(), BigDecimal.ZERO),
                p.getStatus())).toList();
    }
}
