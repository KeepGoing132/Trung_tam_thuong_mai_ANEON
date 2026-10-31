package com.company.aneon.f10.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.company.aneon.f10.integration.NotificationPort;

/**
 * Dispatch thong bao SAU commit (AFTER_COMMIT): nghiep vu chinh khong doi
 * provider; that bai dispatch khong rollback nghiep vu (rule 06).
 */
@Component
public class EventNotificationDispatcher {

    private final NotificationPort notificationPort;

    public EventNotificationDispatcher(NotificationPort notificationPort) {
        this.notificationPort = notificationPort;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onEventApproval(EventApprovalNotification notification) {
        notificationPort.notifyEventApproval(notification.eventId(),
                notification.decision(), notification.recipientId());
    }
}
