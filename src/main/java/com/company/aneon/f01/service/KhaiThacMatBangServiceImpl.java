package com.company.aneon.f01.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.company.aneon.f01.constant.PremiseStatus;
import com.company.aneon.f01.dto.UtilizationDtos;
import com.company.aneon.f01.entity.KhuVuc;
import com.company.aneon.f01.entity.Tang;
import com.company.aneon.f01.mapper.KhaiThacMapper;
import com.company.aneon.f01.repository.KhuVucRepository;
import com.company.aneon.f01.repository.MatBangQueryDao;
import com.company.aneon.f01.repository.TangRepository;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.security.AccessScopePort;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * F01UC05.1 - 05.4. Zone va floor mau thuan khong tra du lieu sai (rule f01);
 * summary tinh tren toan bo tap da loc, khong chi trang hien tai.
 */
@Service
public class KhaiThacMatBangServiceImpl implements KhaiThacMatBangService {

    private final MatBangQueryDao matBangQueryDao;
    private final KhuVucRepository khuVucRepository;
    private final TangRepository tangRepository;
    private final KhaiThacMapper mapper;
    private final AccessScopePort accessScopePort;

    public KhaiThacMatBangServiceImpl(MatBangQueryDao matBangQueryDao, KhuVucRepository khuVucRepository,
                                      TangRepository tangRepository, KhaiThacMapper mapper,
                                      AccessScopePort accessScopePort) {
        this.matBangQueryDao = matBangQueryDao;
        this.khuVucRepository = khuVucRepository;
        this.tangRepository = tangRepository;
        this.mapper = mapper;
        this.accessScopePort = accessScopePort;
    }

    @Override
    @Transactional(readOnly = true)
    public UtilizationDtos.UtilizationPage getUtilization(UUID zoneId, UUID floorId,
                                                          PremiseStatus status, int page, int size) {
        if (floorId != null) {
            Tang floor = tangRepository.findById(floorId)
                    .orElseThrow(() -> ApiException.notFound("Tang", floorId));
            if (zoneId != null) {
                KhuVuc zone = khuVucRepository.findById(zoneId)
                        .orElseThrow(() -> ApiException.notFound("Khu vuc", zoneId));
                if (!floor.getId().equals(zone.getFloor().getId())) {
                    throw ApiException.validation("zoneId", "Khu vuc khong thuoc tang da chon");
                }
            }
        }
        if (zoneId != null) {
            KhuVuc zone = khuVucRepository.findById(zoneId)
                    .orElseThrow(() -> ApiException.notFound("Khu vuc", zoneId));
            if (!zone.isActive()) {
                throw ApiException.validation("zoneId", "Khu vuc khong con hoat dong");
            }
            if (!accessScopePort.currentScope().allowsZone(zoneId)) {
                throw ApiException.forbidden("Du lieu nam ngoai pham vi duoc cap quyen");
            }
        }

        var scope = accessScopePort.currentScope();
        var filter = new MatBangQueryDao.PremiseFilter(null, zoneId, floorId, status, scope);
        var pageResult = matBangQueryDao.search(filter, PageRequest.of(page, size));

        Map<PremiseStatus, Long> counts = matBangQueryDao.countByStatus(filter);
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        UtilizationDtos.UtilizationSummary summary = new UtilizationDtos.UtilizationSummary(
                total,
                counts.getOrDefault(PremiseStatus.LEASED, 0L),
                counts.getOrDefault(PremiseStatus.VACANT, 0L),
                counts.getOrDefault(PremiseStatus.MAINTENANCE, 0L));

        List<UtilizationDtos.UtilizationItem> content = mapper.toItems(pageResult.getContent());
        return new UtilizationDtos.UtilizationPage(summary, content,
                pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }
}
