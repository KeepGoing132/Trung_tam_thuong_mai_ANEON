package com.company.aneon.f10.integration;

import java.util.Optional;
import java.util.UUID;

/**
 * Port doc du lieu F01 can cho F10 (vi tri, cua hang). F10 khong sua du lieu
 * F01 qua port nay (rule 06). lockLocation dung khi kiem tra trung lich sanh:
 * khoa dong hang vi_tri truoc khi kiem tra/luu hoat dong (rule 03).
 */
public interface F01LookupPort {

    record LocationRef(UUID id, UUID zoneId, String textDescription, boolean archived) {
    }

    record StoreRef(UUID id, String code, UUID zoneId, boolean archived) {
    }

    Optional<LocationRef> findLocation(UUID locationId);

    Optional<StoreRef> findStore(UUID storeId);

    /** Khoa vi tri (SELECT ... FOR UPDATE); false neu vi tri khong ton tai. */
    boolean lockLocation(UUID locationId);
}
