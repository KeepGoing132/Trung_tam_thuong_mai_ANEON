package com.company.aneon.f10.integration;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * STUB CO NHAN - chi profile local/test: ghi log thay vi goi F15.
 * Khong bao la thong bao da gui that; khi tich hop F15 thay bean nay.
 */
@Component
@Profile({"local", "test"})
public class LocalLoggingNotificationAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(LocalLoggingNotificationAdapter.class);

    @Override
    public void notifyEventApproval(UUID eventId, String decision, UUID recipientId) {
        log.info("[stub-thong-bao] su kien {} {} thong bao cho {}", eventId, decision, recipientId);
    }
}
