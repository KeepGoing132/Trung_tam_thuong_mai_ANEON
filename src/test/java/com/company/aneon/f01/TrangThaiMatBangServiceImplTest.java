package com.company.aneon.f01;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.company.aneon.f01.constant.PremiseStatus;
import com.company.aneon.f01.dto.StatusDtos;
import com.company.aneon.f01.entity.KhuVuc;
import com.company.aneon.f01.entity.MatBang;
import com.company.aneon.f01.entity.Tang;
import com.company.aneon.f01.entity.ToaNha;
import com.company.aneon.f01.integration.ContractQueryPort;
import com.company.aneon.f01.mapper.MatBangMapper;
import com.company.aneon.f01.repository.MatBangQueryDao;
import com.company.aneon.f01.repository.MatBangRepository;
import com.company.aneon.f01.service.HistoryRecorder;
import com.company.aneon.f01.service.TrangThaiMatBangServiceImpl;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.error.ErrorCode;
import com.company.aneon.shared.security.AccessScope;
import com.company.aneon.shared.security.AccessScopePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrangThaiMatBangServiceImplTest {

    private MatBangRepository matBangRepository;
    private ContractQueryPort contractQueryPort;
    private TrangThaiMatBangServiceImpl service;

    private final UUID actorId = UUID.randomUUID();
    private final UUID premiseId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        matBangRepository = mock(MatBangRepository.class);
        contractQueryPort = mock(ContractQueryPort.class);
        AccessScopePort accessScopePort = mock(AccessScopePort.class);
        when(accessScopePort.currentScope()).thenReturn(AccessScope.ALL);
        when(accessScopePort.currentActorId()).thenReturn(Optional.of(actorId));

        ToaNha building = new ToaNha(UUID.randomUUID(), "TN01", "Toa nha 1", null, actorId);
        Tang floor = new Tang();
        floor.setId(UUID.randomUUID());
        floor.setBuilding(building);
        KhuVuc zone = new KhuVuc();
        zone.setId(UUID.randomUUID());
        zone.setFloor(floor);

        MatBang premise = new MatBang();
        premise.setId(premiseId);
        premise.setCode("MB001");
        premise.setName("MB 001");
        premise.setZone(zone);
        premise.setTotalArea(new BigDecimal("100"));
        premise.setStatus(PremiseStatus.LEASED);
        org.springframework.test.util.ReflectionTestUtils.setField(premise, "version", 5L);
        when(matBangRepository.findWithLockById(premiseId)).thenReturn(Optional.of(premise));
        when(matBangRepository.findById(premiseId)).thenReturn(Optional.of(premise));

        service = new TrangThaiMatBangServiceImpl(matBangRepository, mock(MatBangQueryDao.class),
                contractQueryPort, mock(HistoryRecorder.class), new MatBangMapper(),
                accessScopePort, Clock.systemUTC());
    }

    private StatusDtos.ChangeStatusRequest request(PremiseStatus status, Long version) {
        return new StatusDtos.ChangeStatusRequest(status, "ly do", version);
    }

    @Test
    void leasedSangRetiredHopLeTheoMaTran() {
        StatusDtos.StatusChangeResult result = service.updatePremiseStatus(premiseId,
                request(PremiseStatus.RETIRED, 5L));
        assertThat(result.previousStatus()).isEqualTo(PremiseStatus.LEASED);
        assertThat(result.currentStatus()).isEqualTo(PremiseStatus.RETIRED);
    }

    @Test
    void retiredKhongQuayLaiNem409() {
        MatBang premise = matBangRepository.findById(premiseId).orElseThrow();
        premise.setStatus(PremiseStatus.RETIRED);
        assertThatThrownBy(() -> service.updatePremiseStatus(premiseId, request(PremiseStatus.VACANT, 5L)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.BUSINESS_CONFLICT);
    }

    @Test
    void doiSangVacantKhiConHopDongNem409() {
        when(contractQueryPort.isAvailable()).thenReturn(true);
        when(contractQueryPort.findActiveContractsForPremise(premiseId))
                .thenReturn(List.of(new ContractQueryPort.ContractSummary(
                        UUID.randomUUID(), "ACTIVE", premiseId, null, null, null)));
        assertThatThrownBy(() -> service.updatePremiseStatus(premiseId, request(PremiseStatus.VACANT, 5L)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("hop dong hieu luc");
    }

    @Test
    void doiSangVacantKhiPortF03LoiNem503() {
        when(contractQueryPort.isAvailable()).thenReturn(false);
        assertThatThrownBy(() -> service.updatePremiseStatus(premiseId, request(PremiseStatus.VACANT, 5L)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.DEPENDENCY_UNAVAILABLE);
    }

    @Test
    void versionCuNem409() {
        assertThatThrownBy(() -> service.updatePremiseStatus(premiseId, request(PremiseStatus.MAINTENANCE, 4L)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("version");
    }

    @Test
    void dongBoHopDongACTIVEChoLEASED() {
        UUID contractId = UUID.randomUUID();
        when(contractQueryPort.isAvailable()).thenReturn(true);
        when(contractQueryPort.findContract(contractId)).thenReturn(Optional.of(
                new ContractQueryPort.ContractSummary(contractId, "ACTIVE", premiseId, null, null, null)));
        StatusDtos.SyncStatusResult result = service.syncStatusFromContract(premiseId,
                new StatusDtos.SyncContractStatusRequest(contractId));
        assertThat(result.currentStatus()).isEqualTo(PremiseStatus.LEASED);
    }

    @Test
    void dongBoHopDongCuaMatBangKhacNem409() {
        UUID contractId = UUID.randomUUID();
        when(contractQueryPort.isAvailable()).thenReturn(true);
        when(contractQueryPort.findContract(contractId)).thenReturn(Optional.of(
                new ContractQueryPort.ContractSummary(contractId, "ACTIVE", UUID.randomUUID(), null, null, null)));
        assertThatThrownBy(() -> service.syncStatusFromContract(premiseId,
                new StatusDtos.SyncContractStatusRequest(contractId)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("khong thuoc mat bang nay");
    }

    @Test
    void dongBoHopDongKhongTonTaiNem404() {
        UUID contractId = UUID.randomUUID();
        when(contractQueryPort.isAvailable()).thenReturn(true);
        when(contractQueryPort.findContract(contractId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.syncStatusFromContract(premiseId,
                new StatusDtos.SyncContractStatusRequest(contractId)))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
