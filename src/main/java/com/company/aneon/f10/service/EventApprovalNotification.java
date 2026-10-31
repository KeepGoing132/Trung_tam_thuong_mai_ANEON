package com.company.aneon.f10.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;

/**
 * Su kien ung dung phat sau commit (AFTER_COMMIT) cho thong bao duyet.
 * Khong giu khoa DB khi goi provider (rule 06).
 */
public record EventApprovalNotification(UUID eventId, UUID recipientId, String decision) {

    public static EventApprovalNotification of(UUID eventId, UUID recipientId, String decision) {
        return new EventApprovalNotification(eventId, recipientId, decision);
    }

    public void publishWith(ApplicationEventPublisher publisher) {
        publisher.publishEvent(this);
    }
}
