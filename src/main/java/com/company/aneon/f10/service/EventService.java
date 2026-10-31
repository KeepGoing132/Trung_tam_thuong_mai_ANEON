package com.company.aneon.f10.service;

import java.util.UUID;

import com.company.aneon.f10.dto.EventDtos;

public interface EventService {

    /** F10UC01.1: tao va gui de xuat -> PENDING_APPROVAL (D0004). */
    EventDtos.EventSummary createProposal(EventDtos.CreateEventProposalRequest request);

    /** F10UC01.5: duyet/tu choi de xuat. */
    EventDtos.EventDetail approve(UUID eventId, EventDtos.ApproveEventRequest request);

    /** F10UC01.2: them loat hoat dong, kiem tra trung vi tri/khung gio. */
    EventDtos.ActivityCreatedResult addActivities(UUID eventId, EventDtos.CreateActivitiesRequest request);

    /** Chi tiet su kien. */
    EventDtos.EventDetail detail(UUID eventId);

    /** Ghi chi phi thuc te (bo tro F10UC01.4). */
    EventDtos.ExpenseCreatedResult recordExpense(UUID eventId, EventDtos.CreateExpenseRequest request);
}
