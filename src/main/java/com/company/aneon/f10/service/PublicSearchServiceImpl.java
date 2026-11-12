package com.company.aneon.f10.service;

import java.time.Clock;
import java.util.List;

import com.company.aneon.f10.dto.PublicDtos;
import com.company.aneon.f10.repository.PublicSearchQueryDao;
import com.company.aneon.shared.error.ApiException;

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
    public PublicDtos.PublicSearchResponse search(String q, String category, String availability, String type, int page, int size) {
        String normalizedCategory = normalized(category);
        if (normalizedCategory != null && !normalizedCategory.equals("EVENT")
                && !normalizedCategory.equals("PROMOTION")) {
            throw ApiException.validation("category", "Category phai la EVENT hoac PROMOTION");
        }
        String normalizedAvailability = normalized(availability);
        if (normalizedAvailability != null && !List.of("CURRENT", "UPCOMING").contains(normalizedAvailability)) {
            throw ApiException.validation("availability", "Trang thai phai la CURRENT hoac UPCOMING");
        }
        var filters = new PublicSearchQueryDao.Filters(q, normalizedCategory, normalizedAvailability, normalized(type));
        java.time.Instant now = java.time.Instant.now(clock);
        long total = publicSearchQueryDao.count(filters, now);
        long offset = (long) page * size;
        List<PublicDtos.PublicItem> content = total <= offset || offset > Integer.MAX_VALUE ? List.of()
                : publicSearchQueryDao.searchPage(filters, now, size, (int) offset).stream()
                        .map(row -> new PublicDtos.PublicItem(row.id(), row.title(), row.category(),
                                row.type(), row.startAt(), row.endAt(), row.location(), row.terms(),
                                row.startAt().isAfter(now) ? "UPCOMING" : "CURRENT"))
                        .toList();
        return new PublicDtos.PublicSearchResponse(content, page, size, total);
    }

    private static String normalized(String value) {
        return value == null || value.isBlank() ? null : value.strip().toUpperCase(java.util.Locale.ROOT);
    }
}
