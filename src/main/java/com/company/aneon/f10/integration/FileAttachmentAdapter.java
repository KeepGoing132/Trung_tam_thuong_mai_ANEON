package com.company.aneon.f10.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.company.aneon.shared.reference.TepDinhKemLookupRepository;

import org.springframework.stereotype.Component;

/**
 * Adapter that su doc tep_dinh_kem (bang dung chung trong baseline).
 */
@Component
public class FileAttachmentAdapter implements FileAttachmentPort {

    private final TepDinhKemLookupRepository attachmentRepository;

    public FileAttachmentAdapter(TepDinhKemLookupRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    public List<AttachmentInfo> validateKeys(List<String> storageKeys, UUID actorId,
                                             long maxFileBytes, List<String> allowedExtensions, int maxFiles) {
        List<AttachmentInfo> valid = new ArrayList<>();
        if (storageKeys == null || storageKeys.isEmpty()) {
            return valid;
        }
        if (storageKeys.size() > maxFiles) {
            return valid;
        }
        for (String key : storageKeys) {
            if (key == null || key.isBlank()) {
                continue;
            }
            var record = attachmentRepository.findByStorageKey(key.trim());
            if (record.isEmpty()) {
                continue;
            }
            var attachment = record.get();
            if (actorId == null || !actorId.equals(attachment.getUploadedBy())) {
                continue;
            }
            String name = attachment.getOriginalName() == null ? "" : attachment.getOriginalName();
            int dotIndex = name.lastIndexOf('.');
            String ext = dotIndex < 0 ? "" : name.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
            if (!allowedExtensions.contains(ext)) {
                continue;
            }
            Long size = attachment.getSizeBytes();
            if (size == null || size <= 0 || size > maxFileBytes) {
                continue;
            }
            valid.add(new AttachmentInfo(key.trim(), name, size));
        }
        return valid;
    }
}
