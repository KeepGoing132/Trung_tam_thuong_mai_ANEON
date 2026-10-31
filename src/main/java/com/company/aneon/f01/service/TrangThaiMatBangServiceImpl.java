package com.company.aneon.f01.service;

import java.time.Clock;
import java.util.UUID;

import com.company.aneon.f01.constant.HistoryAction;
import com.company.aneon.f01.constant.HistoryTargetType;
import com.company.aneon.f01.constant.PremiseStatus;
import com.company.aneon.f01.constant.StatusTransition;
import com.company.aneon.f01.dto.StatusDtos;
import com.company.aneon.f01.entity.MatBang;
import com.company.aneon.f01.integration.ContractQueryPort;
import com.company.aneon.f01.mapper.MatBangMapper;
import com.company.aneon.f01.repository.MatBangQueryDao;
import com.company.aneon.f01.repository.MatBangRepository;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.error.ErrorCode;
import com.company.aneon.shared.security.AccessScopePort;
import com.company.aneon.shared.web.PageResponse;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * F01UC03.1 - 03.3: trang thai mat bang. Khong doi tay thanh VACANT khi con
 * hop dong hieu luc; F03 loi khong duoc coi nhu khong co hop dong (rule f01).
 */
@Service
public class TrangThaiMatBangServiceImpl implements TrangThaiMatBangService {

    /** Ma trang thai hop dong (mo hinh): hop co hieu luc chiem mat bang. */
    private static final String CONTRACT_ACTIVE = "ACTIVE";
    private static final String CONTRACT_SIGNED = "SIGNED";

    private final MatBangRepository matBangRepository;
    private final MatBangQueryDao matBangQueryDao;
    private final ContractQueryPort contractQueryPort;
    private final HistoryRecorder historyRecorder;
    private final MatBangMapper mapper;
    private final AccessScopePort accessScopePort;
    private final Clock clock;

    public TrangThaiMatBangServiceImpl(MatBangRepository matBangRepository, MatBangQueryDao matBangQueryDao,
                                       ContractQueryPort contractQueryPort, HistoryRecorder historyRecorder,
                                       MatBangMapper mapper, AccessScopePort accessScopePort, Clock clock) {
        this.matBangRepository = matBangRepository;
        this.matBangQueryDao = matBangQueryDao;
        this.contractQueryPort = contractQueryPort;
        this.historyRecorder = historyRecorder;
        this.mapper = mapper;
        this.accessScopePort = accessScopePort;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StatusDtos.StatusItem> getCurrentStatuses(PremiseStatus status, int page, int size) {
        var result = matBangQueryDao.search(
                new MatBangQueryDao.PremiseFilter(null, null, null, status, accessScopePort.currentScope()),
                PageRequest.of(page, size));
        return PageResponse.of(result.map(m -> new StatusDtos.StatusItem(
                m.getId(), m.getCode(), m.getName(), m.getStatus())));
    }

    @Override
    @Transactional
    public StatusDtos.StatusChangeResult updatePremiseStatus(UUID id, StatusDtos.ChangeStatusRequest request) {
        MatBang premise = matBangRepository.findWithLockById(id)
                .orElseThrow(() -> ApiException.notFound("Mat bang", id));
        guard(premise.getZone().getId());
        if (request.version() != premise.getVersion()) {
            throw ApiException.conflict("Phien ban du lieu da thay doi (version=" + premise.getVersion() + ")");
        }
        StatusTransition.assertCanTransition(premise.getStatus(), request.status(), "mat bang");
        if (request.status() == PremiseStatus.VACANT) {
            assertNoActiveContract(id);
        }

        MatBangMapper.Snapshot before = mapper.toSnapshot(premise);
        PremiseStatus previous = premise.getStatus();
        premise.setStatus(request.status());
        matBangRepository.save(premise);
        historyRecorder.record(HistoryTargetType.MAT_BANG, id, HistoryAction.UPDATE_STATUS,
                before, mapper.toSnapshot(premise), actorId(), request.reason());
        return new StatusDtos.StatusChangeResult(id, premise.getCode(), previous, premise.getStatus(),
                premise.getVersion());
    }

    @Override
    @Transactional
    public StatusDtos.SyncStatusResult syncStatusFromContract(UUID id, StatusDtos.SyncContractStatusRequest request) {
        MatBang premise = matBangRepository.findWithLockById(id)
                .orElseThrow(() -> ApiException.notFound("Mat bang", id));
        guard(premise.getZone().getId());
        requirePort();

        ContractQueryPort.ContractSummary contract = contractQueryPort.findContract(request.contractId())
                .orElseThrow(() -> ApiException.notFound("Hop dong", request.contractId()));
        // Khong tin contractId client: hop dong phai thuoc dung mat bang
        if (!id.equals(contract.premiseId())) {
            throw ApiException.conflict("Hop dong khong thuoc mat bang nay");
        }

        PremiseStatus previous = premise.getStatus();
        PremiseStatus target = mapContractStatus(contract, id);
        if (target != null && target != previous) {
            StatusTransition.assertCanTransition(previous, target, "mat bang");
            MatBangMapper.Snapshot before = mapper.toSnapshot(premise);
            premise.setStatus(target);
            matBangRepository.save(premise);
            historyRecorder.record(HistoryTargetType.MAT_BANG, id, HistoryAction.SYNC_STATUS,
                    before, mapper.toSnapshot(premise), actorId(),
                    "Dong bo theo hop dong " + contract.id());
            return new StatusDtos.SyncStatusResult(premise.getCode(), previous, target);
        }
        return new StatusDtos.SyncStatusResult(premise.getCode(), previous, previous);
    }

    /**
     * ACTIVE -> LEASED; SIGNED -> RESERVED; ket thuc -> VACANT khi khong con
     * hop dong khac va mat bang khong dang bao tri/ngung khai thac (D0005).
     */
    private PremiseStatus mapContractStatus(ContractQueryPort.ContractSummary contract, UUID premiseId) {
        return switch (contract.status() == null ? "" : contract.status()) {
            case CONTRACT_ACTIVE -> PremiseStatus.LEASED;
            case CONTRACT_SIGNED -> PremiseStatus.RESERVED;
            case "EXPIRED", "TERMINATED", "CANCELLED" -> {
                boolean otherActive = !contractQueryPort
                        .findActiveContractsForPremiseExcluding(premiseId, contract.id()).isEmpty();
                PremiseStatus current = matBangRepository.findById(premiseId).map(MatBang::getStatus)
                        .orElse(PremiseStatus.VACANT);
                if (otherActive || current == PremiseStatus.MAINTENANCE
                        || current == PremiseStatus.SUSPENDED || current == PremiseStatus.RETIRED) {
                    yield null;
                }
                yield PremiseStatus.VACANT;
            }
            default -> null;
        };
    }

    private void assertNoActiveContract(UUID premiseId) {
        requirePort();
        if (!contractQueryPort.findActiveContractsForPremise(premiseId).isEmpty()) {
            throw ApiException.conflict(
                    "Khong the chuyen sang VACANT khi mat bang con hop dong hieu luc");
        }
    }

    private void requirePort() {
        if (!contractQueryPort.isAvailable()) {
            throw ApiException.dependencyUnavailable("Khong truy van duoc hop dong tu F03");
        }
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
