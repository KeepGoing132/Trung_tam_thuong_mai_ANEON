package com.company.aneon.f01.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.UUID;

import com.company.aneon.f01.constant.HistoryAction;
import com.company.aneon.f01.constant.HistoryTargetType;
import com.company.aneon.f01.constant.StatusTransition;
import com.company.aneon.f01.constant.StoreStatus;
import com.company.aneon.f01.dto.StoreDtos;
import com.company.aneon.f01.entity.CuaHangSap;
import com.company.aneon.f01.entity.KhuVuc;
import com.company.aneon.f01.entity.MatBang;
import com.company.aneon.f01.entity.NganhHang;
import com.company.aneon.f01.entity.ViTri;
import com.company.aneon.f01.mapper.CuaHangSapMapper;
import com.company.aneon.f01.repository.CuaHangSapQueryDao;
import com.company.aneon.f01.repository.CuaHangSapRepository;
import com.company.aneon.f01.repository.MatBangRepository;
import com.company.aneon.f01.repository.NganhHangRepository;
import com.company.aneon.f01.repository.ViTriRepository;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.error.ErrorCode;
import com.company.aneon.shared.security.AccessScopePort;
import com.company.aneon.shared.web.PageResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * F01UC02.1 - 02.4: cua hang/sap. Khoa mat bang truoc tinh tong dien tich;
 * chuyen giua hai mat bang khoa ca hai theo ID co dinh (rule 03).
 */
@Service
public class CuaHangSapServiceImpl implements CuaHangSapService {

    private final CuaHangSapRepository cuaHangSapRepository;
    private final CuaHangSapQueryDao cuaHangSapQueryDao;
    private final MatBangRepository matBangRepository;
    private final NganhHangRepository nganhHangRepository;
    private final ViTriRepository viTriRepository;
    private final HistoryRecorder historyRecorder;
    private final CuaHangSapMapper mapper;
    private final AccessScopePort accessScopePort;
    private final Clock clock;

    public CuaHangSapServiceImpl(CuaHangSapRepository cuaHangSapRepository, CuaHangSapQueryDao cuaHangSapQueryDao,
                                 MatBangRepository matBangRepository, NganhHangRepository nganhHangRepository,
                                 ViTriRepository viTriRepository,
                                 HistoryRecorder historyRecorder, CuaHangSapMapper mapper,
                                 AccessScopePort accessScopePort, Clock clock) {
        this.cuaHangSapRepository = cuaHangSapRepository;
        this.cuaHangSapQueryDao = cuaHangSapQueryDao;
        this.matBangRepository = matBangRepository;
        this.nganhHangRepository = nganhHangRepository;
        this.viTriRepository = viTriRepository;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
        this.accessScopePort = accessScopePort;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StoreDtos.StoreListItem> list(UUID premiseId, StoreStatus status, String q,
                                                      int page, int size) {
        if (premiseId != null) {
            MatBang premise = matBangRepository.findWithZoneFloorById(premiseId)
                    .orElseThrow(() -> ApiException.notFound("Mat bang", premiseId));
            guard(premise.getZone().getId());
        }
        Page<CuaHangSap> result = cuaHangSapQueryDao.search(
                new CuaHangSapQueryDao.StoreFilter(premiseId, status, q, accessScopePort.currentScope()),
                PageRequest.of(page, size));
        return PageResponse.of(result.map(mapper::toListItem));
    }

    @Override
    @Transactional(readOnly = true)
    public StoreDtos.StoreDetail detail(UUID id) {
        CuaHangSap store = cuaHangSapRepository.findById(id)
                .filter(s -> s.getArchivedAt() == null)
                .orElseThrow(() -> ApiException.notFound("Cua hang", id));
        if (store.getPremise() != null) {
            guard(store.getPremise().getZone().getId());
        }
        return mapper.toDetail(store);
    }

    @Override
    @Transactional
    public StoreDtos.StoreListItem create(StoreDtos.CreateStoreRequest request) {
        String code = request.code().trim();
        if (cuaHangSapRepository.existsByCodeIgnoreCase(code)) {
            throw new ApiException(ErrorCode.BUSINESS_CONFLICT, "Ma cua hang da ton tai: " + code);
        }
        UUID actorId = actorId();
        MatBang premise = lockPremise(request.premiseId());
        guard(premise.getZone().getId());
        NganhHang category = resolveCategory(request.categoryId());
        ViTri location = resolveLocation(request.locationId(), premise.getZone());

        // Kiem tra dien tich trong cung transaction da khoa mat bang; tong dien
        // tich phai la locking read de khong doc snapshot cu (rule 03)
        BigDecimal allocated = cuaHangSapRepository.sumAllocatedAreaForUpdate(premise.getId().toString());
        if (allocated.add(request.area()).compareTo(premise.getTotalArea()) > 0) {
            throw ApiException.conflict("Dien tich cua hang vuot dien tich con lai cua mat bang (da phan bo "
                    + allocated + "/" + premise.getTotalArea() + " m2)");
        }

        CuaHangSap store = new CuaHangSap();
        store.setId(UUID.randomUUID());
        store.setCode(code);
        store.setName(request.name().trim());
        store.setPremise(premise);
        store.setArea(request.area());
        store.setCategory(category);
        store.setLocation(location);
        store.setStatus(request.status());
        store.setOperatingHours(request.operatingHours());
        store.setDescription(request.description());
        store.setPublicVisible(Boolean.TRUE.equals(request.publicVisible()));
        store.setCreatedBy(actorId);
        cuaHangSapRepository.save(store);

        historyRecorder.record(HistoryTargetType.CUA_HANG, store.getId(), HistoryAction.CREATE,
                null, mapper.toSnapshot(store), actorId, null);
        return mapper.toListItem(store);
    }

    @Override
    @Transactional
    public StoreDtos.StoreDetail update(UUID id, StoreDtos.UpdateStoreRequest request) {
        CuaHangSap store = cuaHangSapRepository.findWithLockById(id)
                .orElseThrow(() -> ApiException.notFound("Cua hang", id));
        if (store.getArchivedAt() != null) {
            throw ApiException.notFound("Cua hang", id);
        }
        if (request.version() != store.getVersion()) {
            throw ApiException.conflict("Phien ban du lieu da thay doi (version=" + store.getVersion() + ")");
        }
        UUID actorId = actorId();
        MatBang currentPremise = store.getPremise();
        MatBang newPremise = currentPremise;
        if (request.premiseId() != null && (currentPremise == null
                || !request.premiseId().equals(currentPremise.getId()))) {
            newPremise = lockTwoPremises(currentPremise == null ? null : currentPremise.getId(),
                    request.premiseId());
        }
        UUID zoneId = newPremise.getZone().getId();
        guard(zoneId);
        NganhHang category = resolveCategory(request.categoryId());
        ViTri location = resolveLocation(request.locationId(), newPremise.getZone());

        CuaHangSapMapper.Snapshot before = mapper.toSnapshot(store);

        BigDecimal newArea = request.area() == null ? store.getArea() : request.area();
        // Tong dien tich hien tai da tru dien tich ban ghi dang sua (khong cong doi);
        // locking read de tranh snapshot cu khi canh tranh
        BigDecimal allocatedOthers = cuaHangSapRepository.sumAllocatedAreaForUpdate(newPremise.getId().toString());
        if (currentPremise != null && currentPremise.getId().equals(newPremise.getId())) {
            BigDecimal own = store.getArea() == null ? BigDecimal.ZERO : store.getArea();
            allocatedOthers = allocatedOthers.subtract(own);
        }
        if (newArea != null
                && allocatedOthers.add(newArea).compareTo(newPremise.getTotalArea()) > 0) {
            throw ApiException.conflict("Dien tich moi vuot dien tich con lai cua mat bang (da phan bo "
                    + allocatedOthers + "/" + newPremise.getTotalArea() + " m2)");
        }

        store.setName(request.name().trim());
        store.setPremise(newPremise);
        store.setArea(newArea);
        store.setCategory(category);
        store.setLocation(location);
        store.setOperatingHours(request.operatingHours());
        store.setDescription(request.description());
        if (request.publicVisible() != null) {
            store.setPublicVisible(request.publicVisible());
        }
        cuaHangSapRepository.save(store);

        historyRecorder.record(HistoryTargetType.CUA_HANG, id, HistoryAction.UPDATE,
                before, mapper.toSnapshot(store), actorId, null);
        return mapper.toDetail(store);
    }

    @Override
    @Transactional
    public StoreDtos.StoreDetail changeStatus(UUID id, StoreDtos.ChangeStoreStatusRequest request) {
        CuaHangSap store = cuaHangSapRepository.findWithLockById(id)
                .orElseThrow(() -> ApiException.notFound("Cua hang", id));
        if (store.getArchivedAt() != null) {
            throw ApiException.notFound("Cua hang", id);
        }
        guard(store.getPremise() == null ? null : store.getPremise().getZone().getId());
        if (request.version() != store.getVersion()) {
            throw ApiException.conflict("Phien ban du lieu da thay doi (version=" + store.getVersion() + ")");
        }
        StatusTransition.assertCanTransition(store.getStatus(), request.status(), "cua hang");

        CuaHangSapMapper.Snapshot before = mapper.toSnapshot(store);
        store.setStatus(request.status());
        cuaHangSapRepository.save(store);
        historyRecorder.record(HistoryTargetType.CUA_HANG, id, HistoryAction.UPDATE_STATUS,
                before, mapper.toSnapshot(store), actorId(), request.reason());
        return mapper.toDetail(store);
    }

    private MatBang lockPremise(UUID premiseId) {
        MatBang premise = matBangRepository.findWithLockById(premiseId)
                .orElseThrow(() -> ApiException.notFound("Mat bang", premiseId));
        if (premise.getArchivedAt() != null) {
            throw ApiException.notFound("Mat bang", premiseId);
        }
        return premise;
    }

    /** Khoa hai mat bang theo thu tu ID co dinh de tranh khoa cheo. */
    private MatBang lockTwoPremises(UUID currentId, UUID newId) {
        if (currentId == null || currentId.equals(newId)) {
            return lockPremise(newId);
        }
        UUID first = currentId.compareTo(newId) < 0 ? currentId : newId;
        UUID second = first.equals(currentId) ? newId : currentId;
        lockPremise(first);
        return lockPremise(second);
    }

    private NganhHang resolveCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        NganhHang category = nganhHangRepository.findById(categoryId)
                .orElseThrow(() -> ApiException.notFound("Nganh hang", categoryId));
        if (category.getArchivedAt() != null) {
            throw ApiException.validation("categoryId", "Nganh hang da luu tru");
        }
        return category;
    }

    private ViTri resolveLocation(UUID locationId, KhuVuc zone) {
        if (locationId == null) {
            return null;
        }
        ViTri location = viTriRepository.findByIdAndArchivedAtIsNull(locationId)
                .orElseThrow(() -> ApiException.notFound("Vi tri", locationId));
        if (!location.getZone().getId().equals(zone.getId())) {
            throw ApiException.validation("locationId", "Vi tri khong thuoc khu vuc cua mat bang");
        }
        return location;
    }

    private void guard(UUID zoneId) {
        if (zoneId != null && !accessScopePort.currentScope().allowsZone(zoneId)) {
            throw ApiException.forbidden("Du lieu nam ngoai pham vi duoc cap quyen");
        }
    }

    private UUID actorId() {
        return accessScopePort.currentActorId().orElseThrow(
                () -> new ApiException(ErrorCode.UNAUTHENTICATED, "Khong xac dinh duoc nguoi thao tac"));
    }
}
