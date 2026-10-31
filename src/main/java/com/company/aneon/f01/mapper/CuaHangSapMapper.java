package com.company.aneon.f01.mapper;

import java.math.BigDecimal;

import com.company.aneon.f01.dto.StoreDtos;
import com.company.aneon.f01.entity.CuaHangSap;

import org.springframework.stereotype.Component;

/**
 * Mapper cua hang/sap: chi chuyen du lieu.
 */
@Component
public class CuaHangSapMapper {

    public record Snapshot(String code, String name, java.util.UUID premiseId, BigDecimal area,
                           java.util.UUID categoryId, String status, java.util.UUID locationId,
                           boolean publicVisible) {
    }

    public StoreDtos.StoreListItem toListItem(CuaHangSap store) {
        return new StoreDtos.StoreListItem(
                store.getId(), store.getCode(), store.getName(),
                store.getPremise() == null ? null : store.getPremise().getId(),
                store.getPremise() == null ? null : store.getPremise().getCode(),
                store.getArea(),
                store.getCategory() == null ? null : store.getCategory().getId(),
                store.getCategory() == null ? null : store.getCategory().getName(),
                store.getStatus(), store.isPublicVisible());
    }

    public StoreDtos.StoreDetail toDetail(CuaHangSap store) {
        return new StoreDtos.StoreDetail(
                store.getId(), store.getCode(), store.getName(),
                store.getPremise() == null ? null : store.getPremise().getId(),
                store.getPremise() == null ? null : store.getPremise().getCode(),
                store.getArea(),
                store.getCategory() == null ? null : store.getCategory().getId(),
                store.getCategory() == null ? null : store.getCategory().getName(),
                store.getLocation() == null ? null : store.getLocation().getId(),
                store.getStatus(), store.getOperatingHours(), store.getDescription(),
                store.isPublicVisible(), store.getVersion());
    }

    public Snapshot toSnapshot(CuaHangSap store) {
        return new Snapshot(store.getCode(), store.getName(),
                store.getPremise() == null ? null : store.getPremise().getId(),
                store.getArea(),
                store.getCategory() == null ? null : store.getCategory().getId(),
                store.getStatus().name(),
                store.getLocation() == null ? null : store.getLocation().getId(),
                store.isPublicVisible());
    }
}
