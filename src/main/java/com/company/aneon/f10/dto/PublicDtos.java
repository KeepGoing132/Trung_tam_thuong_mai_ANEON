package com.company.aneon.f10.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO cong khai F10UC03. Chi chua du lieu public: khong co ngan sach, chi
 * phi, nguoi duyet, QR, redis key hay du lieu khach (rule 05).
 */
public final class PublicDtos {

    private PublicDtos() {
    }

    /** Hang gop tu truy van UNION trong PublicSearchQueryDao. */
    public record PublicRow(UUID id, String title, String category, String type,
                            Instant startAt, Instant endAt, String location) {
    }

    public record PublicItem(UUID id, String title, String category, String type,
                             Instant startAt, Instant endAt, String location) {
    }

    public record PublicSearchResponse(List<PublicItem> content, int page, int size, long totalElements) {
    }
}
