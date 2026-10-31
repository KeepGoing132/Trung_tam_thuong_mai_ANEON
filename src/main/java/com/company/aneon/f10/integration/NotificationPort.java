package com.company.aneon.f10.integration;

import java.util.UUID;

/**
 * Port thong bao F15. Thuc thi sau commit (xu ly su kien AFTER_COMMIT),
 * that bai provider khong duoc anh huong nghiep vu chinh (rule 06).
 */
public interface NotificationPort {

    void notifyEventApproval(UUID eventId, String decision, UUID recipientId);
}
