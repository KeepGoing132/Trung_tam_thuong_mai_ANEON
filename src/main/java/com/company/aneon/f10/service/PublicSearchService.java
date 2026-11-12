package com.company.aneon.f10.service;

import com.company.aneon.f10.dto.PublicDtos;

public interface PublicSearchService {

    /** F10UC03: tra cuu cong khai, phan trang hop nhat EVENT/PROMOTION. */
    PublicDtos.PublicSearchResponse search(String q, String category, String availability, String type, int page, int size);
}
