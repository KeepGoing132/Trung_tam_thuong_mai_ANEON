package com.company.aneon.shared.web;

import java.util.List;

/**
 * Trang ket qua dat trong data theo hop dong; sort phai on dinh (co id).
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements) {

    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements());
    }

    public static <T> PageResponse<T> of(List<T> content, org.springframework.data.domain.Page<?> page) {
        return new PageResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
