package com.company.aneon.f10.service;

import java.util.UUID;

import com.company.aneon.f10.dto.EventDtos;

public interface QrCodeService {

    /** F10UC01.3: sinh QR cho su kien da duyet; QR moi thu hoi QR cu. */
    com.company.aneon.f10.dto.EventDtos.QrCodeDto generate(UUID eventId);

    /** Check-in qua token QR (bo tro F10UC01.3/01.4). */
    EventDtos.CheckInResult checkIn(UUID eventId, EventDtos.CheckInRequest request);
}
