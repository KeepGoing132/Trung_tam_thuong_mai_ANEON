package com.company.aneon.f01.constant;

/**
 * Trang thai mat bang theo mo hinh PlantUML (D0001).
 * Nhan tieng Viet: VACANT=Trong, RESERVED=Da dat, LEASED=Dang khai thac,
 * MAINTENANCE=Bao tri, SUSPENDED=Ngung khai thac, RETIRED=Khong su dung.
 * Cac ma TRONG/DANG_THUE/DA_THUE... trong Word chua duoc nhan lam gia tri API.
 */
public enum PremiseStatus {
    VACANT,
    RESERVED,
    LEASED,
    MAINTENANCE,
    SUSPENDED,
    RETIRED
}
