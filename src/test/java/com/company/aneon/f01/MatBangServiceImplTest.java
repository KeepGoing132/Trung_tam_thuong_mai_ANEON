package com.company.aneon.f01;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.company.aneon.f01.constant.PremiseStatus;
import com.company.aneon.f01.dto.PremiseDtos;
import com.company.aneon.f01.entity.KhuVuc;
import com.company.aneon.f01.entity.MatBang;
import com.company.aneon.f01.entity.Tang;
import com.company.aneon.f01.entity.ToaNha;
import com.company.aneon.f01.integration.DigitalMapPort;
import com.company.aneon.f01.mapper.MatBangMapper;
import com.company.aneon.f01.repository.CuaHangSapRepository;
import com.company.aneon.f01.repository.KhuVucRepository;
import com.company.aneon.f01.repository.MatBangQueryDao;
import com.company.aneon.f01.repository.MatBangRepository;
import com.company.aneon.f01.repository.ViTriRepository;
import com.company.aneon.f01.service.HistoryRecorder;
import com.company.aneon.f01.service.MatBangServiceImpl;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.security.AccessScope;
import com.company.aneon.shared.security.AccessScopePort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MatBangServiceImplTest {

    private MatBangRepository matBangRepository;
    private KhuVucRepository khuVucRepository;
    private CuaHangSapRepository cuaHangSapRepository;
    private AccessScopePort accessScopePort;
    private MatBangServiceImpl service;

    private final UUID zoneId = UUID.randomUUID();
    private final UUID actorId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        matBangRepository = mock(MatBangRepository.class);
        khuVucRepository = mock(KhuVucRepository.class);
        cuaHangSapRepository = mock(CuaHangSapRepository.class);
        accessScopePort = mock(AccessScopePort.class);
        when(accessScopePort.currentScope()).thenReturn(AccessScope.ALL);
        when(accessScopePort.currentActorId()).thenReturn(Optional.of(actorId));

        ToaNha building = new ToaNha(UUID.randomUUID(), "TN01", "Toa nha 1", null, actorId);
        Tang floor = new Tang();
        floor.setId(UUID.randomUUID());
        floor.setBuilding(building);
        floor.setActive(true);
        KhuVuc zone = new KhuVuc();
        zone.setId(zoneId);
        zone.setFloor(floor);
        zone.setActive(true);
        when(khuVucRepository.findByIdAndActiveTrue(zoneId)).thenReturn(Optional.of(zone));

        service = new MatBangServiceImpl(matBangRepository, mock(MatBangQueryDao.class),
                khuVucRepository, mock(ViTriRepository.class), cuaHangSapRepository,
                mock(HistoryRecorder.class), new MatBangMapper(), mock(DigitalMapPort.class),
                accessScopePort, Clock.systemUTC());
    }

    private MatBang premise(String code, BigDecimal totalArea, long version) {
        MatBang premise = new MatBang();
        premise.setId(UUID.randomUUID());
        premise.setCode(code);
        premise.setName("MB " + code);
        premise.setZone(new KhuVuc());
        premise.getZone().setId(zoneId);
        premise.setTotalArea(totalArea);
        premise.setStatus(PremiseStatus.VACANT);
        org.springframework.test.util.ReflectionTestUtils.setField(premise, "version", version);
        return premise;
    }

    @Test
    void taoMatBangMaTrungNem409() {
        when(matBangRepository.existsByCodeIgnoreCase("MB001")).thenReturn(true);
        PremiseDtos.CreatePremiseRequest request = new PremiseDtos.CreatePremiseRequest(
                "MB001", "Mat bang 1", zoneId, new BigDecimal("100"), null, PremiseStatus.VACANT,
                null, null);
        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(com.company.aneon.shared.error.ErrorCode.BUSINESS_CONFLICT);
    }

    @Test
    void suaMatBangVersionCuNem409() {
        MatBang existing = premise("MB001", new BigDecimal("100"), 3);
        when(matBangRepository.findWithLockById(existing.getId())).thenReturn(Optional.of(existing));
        when(cuaHangSapRepository.sumAllocatedAreaForUpdate(existing.getId().toString())).thenReturn(BigDecimal.ZERO);
        PremiseDtos.UpdatePremiseRequest request = new PremiseDtos.UpdatePremiseRequest(
                "Ten moi", zoneId, new BigDecimal("120"), null, null, 2L);
        assertThatThrownBy(() -> service.update(existing.getId(), request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(com.company.aneon.shared.error.ErrorCode.BUSINESS_CONFLICT);
    }

    @Test
    void suaMatBangThuNhoDuoiDienTichDaPhanBoNem409() {
        MatBang existing = premise("MB001", new BigDecimal("100"), 1);
        when(matBangRepository.findWithLockById(existing.getId())).thenReturn(Optional.of(existing));
        when(cuaHangSapRepository.sumAllocatedAreaForUpdate(existing.getId().toString())).thenReturn(new BigDecimal("80"));
        PremiseDtos.UpdatePremiseRequest request = new PremiseDtos.UpdatePremiseRequest(
                "Ten moi", zoneId, new BigDecimal("50"), null, null, 1L);
        assertThatThrownBy(() -> service.update(existing.getId(), request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("phan bo");
    }

    @Test
    void suaMatBangHopLeTraVersionMoi() {
        MatBang existing = premise("MB001", new BigDecimal("100"), 1);
        when(matBangRepository.findWithLockById(existing.getId())).thenReturn(Optional.of(existing));
        when(cuaHangSapRepository.sumAllocatedAreaForUpdate(existing.getId().toString())).thenReturn(new BigDecimal("30"));
        PremiseDtos.UpdatePremiseRequest request = new PremiseDtos.UpdatePremiseRequest(
                "Ten moi", zoneId, new BigDecimal("90"), null, "doi dien tich", 1L);
        PremiseDtos.UpdateResult result = service.update(existing.getId(), request);
        // JPA @Version tang khi flush (kiem tran boi IT); o day mock repository
        // khong flush nen version giu nguyen do service tu cap nhat
        assertThat(existing.getTotalArea()).isEqualByComparingTo(new BigDecimal("90"));
        assertThat(existing.getName()).isEqualTo("Ten moi");
        assertThat(result.id()).isEqualTo(existing.getId());
    }

    @Test
    void codeAvailabilityExcludeIdLayRiengBanGhiDangSua() {
        MatBang existing = premise("MB001", new BigDecimal("100"), 1);
        when(matBangRepository.findByCodeIgnoreCase("MB001")).thenReturn(Optional.of(existing));
        assertThat(service.checkCodeAvailable("MB001", existing.getId()).available()).isTrue();
        assertThat(service.checkCodeAvailable("MB001", UUID.randomUUID()).available()).isFalse();
        assertThat(service.checkCodeAvailable("MB002", null).available()).isTrue();
    }

    @Test
    void codeAvailabilityBoTrongNem400() {
        assertThatThrownBy(() -> service.checkCodeAvailable("  ", null))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(com.company.aneon.shared.error.ErrorCode.VALIDATION_ERROR);
    }
}
