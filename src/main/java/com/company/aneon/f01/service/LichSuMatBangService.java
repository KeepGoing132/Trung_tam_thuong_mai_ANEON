package com.company.aneon.f01.service;

import java.util.UUID;

import com.company.aneon.f01.dto.HistoryDtos;
import com.company.aneon.shared.web.PageResponse;

public interface LichSuMatBangService {

    PageResponse<HistoryDtos.HistoryItem> getPremiseHistory(UUID premiseId, int page, int size);

    PageResponse<HistoryDtos.HistoryItem> searchHistory(HistoryDtos.HistorySearchRequest request, int page, int size);
}
