package com.company.aneon.f10.service;

import java.util.List;
import java.util.UUID;

import com.company.aneon.f10.constant.EventStatus;
import com.company.aneon.f10.dto.EventDtos;
import com.company.aneon.shared.web.PageResponse;

public interface EventService {

    PageResponse<EventDtos.EventSummary> search(String q, EventStatus status, int page, int size);

    /** F10UC01.1: tao va gui de xuat -> PENDING_APPROVAL (D0004). */
    EventDtos.EventSummary createProposal(EventDtos.CreateEventProposalRequest request);

    EventDtos.EventSummary createDraft(EventDtos.CreateEventProposalRequest request);

    EventDtos.EventDetail updateDraft(UUID id, long version, EventDtos.CreateEventProposalRequest request);

    EventDtos.EventDetail submitDraft(UUID id, long version);

    /** F10UC01.5: duyet/tu choi de xuat. */
    EventDtos.EventDetail approve(UUID eventId, EventDtos.ApproveEventRequest request);

    /** F10UC01.2: them loat hoat dong, kiem tra trung vi tri/khung gio. */
    EventDtos.ActivityCreatedResult addActivities(UUID eventId, EventDtos.CreateActivitiesRequest request);

    List<EventDtos.ActivityItem> activities(UUID eventId);

    EventDtos.ActivityItem updateActivity(UUID eventId, UUID activityId, long version, EventDtos.CreateActivityRequest request);

    UUID archiveActivity(UUID eventId, UUID activityId, long version);

    /** Chi tiet su kien. */
    EventDtos.EventDetail detail(UUID eventId);

    /** Ghi chi phi thuc te (bo tro F10UC01.4). */
    EventDtos.ExpenseCreatedResult recordExpense(UUID eventId, EventDtos.CreateExpenseRequest request);
}
