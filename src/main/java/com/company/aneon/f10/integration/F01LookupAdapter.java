package com.company.aneon.f10.integration;

import java.util.Optional;
import java.util.UUID;

import com.company.aneon.f01.entity.ViTri;
import com.company.aneon.f01.repository.CuaHangSapRepository;
import com.company.aneon.f01.repository.MatBangRepository;
import com.company.aneon.f01.repository.ViTriRepository;

import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

/**
 * Adapter that su: doc vi_tri/cua_hang qua repository F01 de phuc vu port.
 * Read-only; khong sua du lieu F01.
 */
@Component
public class F01LookupAdapter implements F01LookupPort {

    private final ViTriRepository viTriRepository;
    private final CuaHangSapRepository cuaHangSapRepository;
    private final EntityManager entityManager;

    public F01LookupAdapter(ViTriRepository viTriRepository, CuaHangSapRepository cuaHangSapRepository,
                            EntityManager entityManager) {
        this.viTriRepository = viTriRepository;
        this.cuaHangSapRepository = cuaHangSapRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<LocationRef> findLocation(UUID locationId) {
        return viTriRepository.findById(locationId)
                .map(v -> new LocationRef(v.getId(), v.getZone().getId(), v.getTextDescription(),
                        v.getArchivedAt() != null));
    }

    @Override
    public Optional<StoreRef> findStore(UUID storeId) {
        return cuaHangSapRepository.findById(storeId)
                .map(s -> new StoreRef(s.getId(), s.getCode(), s.getPremise() == null ? null
                        : s.getPremise().getZone().getId(), s.getArchivedAt() != null));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean lockLocation(UUID locationId) {
        // Khoa dong vi tri dung chung de hai su kien khong dat cung sanh
        int updated = entityManager
                .createNativeQuery("select id from vi_tri where id = :id for update")
                .setParameter("id", locationId.toString())
                .setMaxResults(1)
                .getResultList().size();
        return updated > 0;
    }
}
