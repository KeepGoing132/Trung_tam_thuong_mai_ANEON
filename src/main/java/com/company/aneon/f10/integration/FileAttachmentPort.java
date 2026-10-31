package com.company.aneon.f10.integration;

import java.util.List;
import java.util.UUID;

/**
 * Port kiem tra tep dinh kem cua de xuat su kien: key phai thuoc tep da tai
 * boi cung actor, dung dinh dang (PDF/DOCX/PNG/JPG) va dung luong
 * (<= 25 MiB/file, toi da 10 tep) theo rule f10/D0004.
 */
public interface FileAttachmentPort {

    record AttachmentInfo(String storageKey, String originalName, long sizeBytes) {
    }

    /**
     * Tra danh sach tep hop le; tung key sai la loi nghiep vu do service quyet
     * dinh thong bao. implementation khong nem exception tranh two-phase.
     */
    List<AttachmentInfo> validateKeys(List<String> storageKeys, UUID actorId,
                                      long maxFileBytes, List<String> allowedExtensions, int maxFiles);
}
