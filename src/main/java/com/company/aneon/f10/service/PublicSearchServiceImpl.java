package com.company.aneon.f10.service;

import java.time.Clock;
import java.util.List;

import com.company.aneon.f10.dto.PublicDtos;
import com.company.aneon.f10.repository.PublicSearchQueryDao;
import com.company.aneon.shared.web.PageParams;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * F10UC03: cong khai - chi du lieu publicVisible, chua archived, dung trang
 * thai va con han (rule f10). Khong can xac thuc.
 */
@Service
public class PublicSearchServiceImpl implements PublicSearchService {

    private final PublicSearchQueryDao publicSearchQueryDao;
    private final Clock clock;

    public PublicSearchServiceImpl(PublicSearchQueryDao publicSearchQueryDao, Clock clock) {
        this.publicSearchQueryDao = publicSearchQueryDao;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public PublicDtos.PublicSearchResponse search(String q, String category, int page, int size) {
        String normalizedCategory = category == null ? null : category.trim().toUpperCase();
        if (normalizedCategory != null && !normalizedCategory.equals("EVENT")
                && !normalizedCategory.equals("PROMOTION")) {
            throw com.company.aneon.shared.error.ApiException.validation("category",
                    "Category phai la EVENT hoac PROMOTION");
        }
        java.time.Instant now = java.time.Instant.now(clock);
        long total = publicSearchQueryDao.count(q, normalizedCategory, now);
        int offset = page * size;
        List<PublicDtos.PublicItem> content = total <= offset ? List.of()
                : publicSearchQueryDao.searchPage(q, normalizedCategory, now, size, offset).stream()
                        .map(row -> new PublicDtos.PublicItem(row.id(), row.title(), row.category(),
                                row.type(), row.startAt(), row.endAt(), row.location()))
                        .toList();
        return new PublicDtos.PublicSearchResponse(content, page, size, total);
    }
}
