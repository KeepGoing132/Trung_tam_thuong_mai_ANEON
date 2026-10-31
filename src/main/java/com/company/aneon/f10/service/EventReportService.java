package com.company.aneon.f10.service;

import java.util.UUID;

import com.company.aneon.f10.dto.EventDtos;

public interface EventReportService {

    /** F10UC01.4: bao cao chi phí/lượt tham gia/ROI tu du lieu that. */
    EventDtos.EventReportResponse report(UUID eventId);
}
