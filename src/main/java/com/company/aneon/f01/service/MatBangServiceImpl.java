package com.company.aneon.f01.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;

import com.company.aneon.f01.constant.HistoryAction;
import com.company.aneon.f01.constant.HistoryTargetType;
import com.company.aneon.f01.constant.PremiseStatus;
import com.company.aneon.f01.dto.PremiseDtos;
import com.company.aneon.f01.entity.KhuVuc;
import com.company.aneon.f01.entity.MatBang;
import com.company.aneon.f01.entity.ViTri;
import com.company.aneon.f01.integration.DigitalMapPort;
import com.company.aneon.f01.mapper.MatBangMapper;
import com.company.aneon.f01.repository.CuaHangSapRepository;
import com.company.aneon.f01.repository.KhuVucRepository;
import com.company.aneon.f01.repository.MatBangQueryDao;
import com.company.aneon.f01.repository.MatBangRepository;
import com.company.aneon.f01.repository.ViTriRepository;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.error.ErrorCode;
import com.company.aneon.shared.security.AccessScopePort;
import com.company.aneon.shared.web.PageResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * F01UC01.1 - 01.5: tra cuu, them, sua, kiem tra ma, cap nhat vi tri mat bang.
 */
@Service
public class MatBangServiceImpl implements MatBangService {

    private final MatBangRepository matBangRepository;
    private final MatBangQueryDao matBangQueryDao;
    private final KhuVucRepository khuVucRepository;
    private final ViTriRepository viTriRepository;
    private final CuaHangSapRepository cuaHangSapRepository;
    private final HistoryRecorder historyRecorder;
    private final MatBangMapper mapper;
    private final DigitalMapPort digitalMapPort;
    private final AccessScopePort accessScopePort;
    private final Clock clock;

    public MatBangServiceImpl(MatBangRepository matBangRepository, MatBangQueryDao matBangQueryDao,
                              KhuVucRepository khuVucRepository, ViTriRepository viTriRepository,
                              CuaHangSapRepository cuaHangSapRepository, HistoryRecorder historyRecorder,
                              MatBangMapper mapper, DigitalMapPort digitalMapPort,
                              AccessScopePort accessScopePort, Clock clock) {
        this.matBangRepository = matBangRepository;
        this.matBangQueryDao = matBangQueryDao;
        this.khuVucRepository = khuVucRepository;
        this.viTriRepository = viTriRepository;
        this.cuaHangSapRepository = cuaHangSapRepository;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
        this.digitalMapPort = digitalMapPort;
        this.accessScopePort = accessScopePort;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PremiseDtos.PremiseListItem> list(String q, UUID zoneId, PremiseStatus status,
                                                          int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        MatBangQueryDao.PremiseFilter filter = new MatBangQueryDao.PremiseFilter(
                q, zoneId, null, status, accessScopePort.currentScope());
        Page<MatBang> result = matBangQueryDao.search(filter, pageable);
        return PageResponse.of(result.map(mapper::toListItem));
    }

    @Override
    @Transactional(readOnly = true)
    public PremiseDtos.PremiseDetail detail(UUID id) {
        MatBang premise = matBangRepository.findWithZoneFloorById(id)
                .orElseThrow(() -> ApiException.notFound("Mat bang", id));
        guard(premise.getZone().getId());
        BigDecimal allocated = cuaHangSapRepository.sumAllocatedArea(id);
        return mapper.toDetail(premise, allocated);
    }

    @Override
    @Transactional
    public PremiseDtos.CreatePremiseResult create(PremiseDtos.CreatePremiseRequest request) {
        String code = request.code().trim();
        if (matBangRepository.existsByCodeIgnoreCase(code)) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Ma mat bang da ton tai: " + code);
        }
        KhuVuc zone = khuVucRepository.findByIdAndActiveTrue(request.zoneId())
                .orElseThrow(() -> ApiException.notFound("Khu vuc", request.zoneId()));
        guard(zone.getId());
        ViTri location = resolveLocation(request.locationId(), zone.getId());
        verifyMappable(location);

        UUID actorId = actorId();
        MatBang premise = new MatBang();
        premise.setId(UUID.randomUUID());
        premise.setCode(code);
        premise.setName(request.name().trim());
        premise.setZone(zone);
        premise.setTotalArea(request.totalArea());
        premise.setType(request.type());
        premise.setStatus(request.status());
        premise.setLocation(location);
        premise.setDescription(request.description());
        premise.setCreatedBy(actorId);
        matBangRepository.save(premise);

        historyRecorder.record(HistoryTargetType.MAT_BANG, premise.getId(), HistoryAction.CREATE,
                null, mapper.toSnapshot(premise), actorId, null);
        return new PremiseDtos.CreatePremiseResult(premise.getId(), premise.getCode(), premise.getStatus());
    }

    @Override
    @Transactional
    public PremiseDtos.UpdateResult update(UUID id, PremiseDtos.UpdatePremiseRequest request) {
        MatBang premise = matBangRepository.findWithLockById(id)
                .orElseThrow(() -> ApiException.notFound("Mat bang", id));
        guard(premise.getZone().getId());
        if (request.version() != premise.getVersion()) {
            throw ApiException.conflict("Phien ban du lieu da thay doi (version=" + premise.getVersion() + ")");
        }
        KhuVuc newZone = khuVucRepository.findByIdAndActiveTrue(request.zoneId())
                .orElseThrow(() -> ApiException.notFound("Khu vuc", request.zoneId()));
        guard(newZone.getId());

        MatBangMapper.Snapshot before = mapper.toSnapshot(premise);
        BigDecimal allocated = cuaHangSapRepository.sumAllocatedAreaForUpdate(id.toString());
        if (allocated.compareTo(request.totalArea()) > 0) {
            throw ApiException.conflict("Tong dien tich moi nho hon dien tich da phan bo cho cua hang ("
                    + allocated + " m2)");
        }
        premise.setName(request.name().trim());
        premise.setZone(newZone);
        premise.setTotalArea(request.totalArea());
        premise.setType(request.type());
        premise.setDescription(request.description());
        matBangRepository.save(premise);

        historyRecorder.record(HistoryTargetType.MAT_BANG, id, HistoryAction.UPDATE,
                before, mapper.toSnapshot(premise), actorId(), request.description());
        return new PremiseDtos.UpdateResult(id, premise.getVersion());
    }

    @Override
    @Transactional(readOnly = true)
    public PremiseDtos.CodeAvailability checkCodeAvailable(String code, UUID excludeId) {
        String normalized = code == null ? "" : code.trim();
        if (normalized.isEmpty()) {
            throw ApiException.validation("code", "Ma mat bang khong duoc trong");
        }
        boolean available = matBangRepository.findByCodeIgnoreCase(normalized)
                .map(found -> found.getId().equals(excludeId))
                .orElse(true);
        return new PremiseDtos.CodeAvailability(normalized, available);
    }

    @Override
    @Transactional
    public PremiseDtos.LocationUpdateResult relocate(UUID id, PremiseDtos.UpdatePremiseLocationRequest request) {
        MatBang premise = matBangRepository.findWithLockById(id)
                .orElseThrow(() -> ApiException.notFound("Mat bang", id));
        guard(premise.getZone().getId());
        ViTri location = viTriRepository.findByIdAndArchivedAtIsNull(request.locationId())
                .orElseThrow(() -> ApiException.notFound("Vi tri", request.locationId()));
        if (!location.getZone().getId().equals(premise.getZone().getId())) {
            throw ApiException.validation("locationId", "Vi tri khong thuoc khu vuc cua mat bang");
        }
        verifyMappable(location);

        MatBangMapper.Snapshot before = mapper.toSnapshot(premise);
        premise.setLocation(location);
        matBangRepository.save(premise);
        historyRecorder.record(HistoryTargetType.MAT_BANG, id, HistoryAction.UPDATE_LOCATION,
                before, mapper.toSnapshot(premise), actorId(), request.reason());
        return new PremiseDtos.LocationUpdateResult(id, location.getId());
    }

    private ViTri resolveLocation(UUID locationId, UUID zoneId) {
        if (locationId == null) {
            return null;
        }
        return viTriRepository.findByIdAndZoneId(locationId, zoneId)
                .orElseThrow(() -> ApiException.validation("locationId",
                        "Vi tri khong thuoc khu vuc cua mat bang"));
    }

    private void verifyMappable(ViTri location) {
        if (location == null) {
            return;
        }
        if (!digitalMapPort.isAvailable()) {
            throw ApiException.dependencyUnavailable("Khong xac minh duoc vi tri tren ban do (F18)");
        }
        if (!digitalMapPort.isLocationMappable(location.getId())) {
            throw ApiException.validation("locationId", "Vi tri nam ngoai pham vi ban do TTTM");
        }
    }

    private void guard(UUID zoneId) {
        if (!accessScopePort.currentScope().allowsZone(zoneId)) {
            throw ApiException.forbidden("Du lieu nam ngoai pham vi duoc cap quyen");
        }
    }

    private UUID actorId() {
        return accessScopePort.currentActorId().orElseThrow(
                () -> new ApiException(ErrorCode.UNAUTHENTICATED, "Khong xac dinh duoc nguoi thao tac"));
    }
}
