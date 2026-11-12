package com.company.aneon.f10.service;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.company.aneon.f10.constant.EventStatus;
import com.company.aneon.f10.repository.SuKienRepository;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.security.AccessScopePort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class EventFiles {
    public record FileInfo(UUID id, String name, long sizeBytes) { }
    public record FileContent(String name, byte[] bytes) { }

    private final SuKienRepository events;
    private final AccessScopePort access;
    private final JdbcClient jdbc;
    private final int maxFiles;
    private final long maxBytes;

    public EventFiles(SuKienRepository events, AccessScopePort access, JdbcClient jdbc,
                      @Value("${aneon.upload.max-files-per-proposal:10}") int maxFiles,
                      @Value("${aneon.upload.max-file-bytes:26214400}") long maxBytes) {
        this.events = events;
        this.access = access;
        this.jdbc = jdbc;
        this.maxFiles = maxFiles;
        this.maxBytes = maxBytes;
    }

    @Transactional(readOnly = true)
    public List<FileInfo> list(UUID eventId) {
        requireRead(eventId);
        return jdbc.sql("""
                select f.id, f.original_name, f.size_bytes
                from tep_dinh_kem f join noi_dung_tep_de_xuat c on c.attachment_id = f.id
                where f.target_type = 'SU_KIEN' and f.target_id = :event and f.archived_at is null
                order by f.created_at, f.id
                """).param("event", eventId.toString()).query((row, index) -> new FileInfo(
                UUID.fromString(row.getString("id")), row.getString("original_name"), row.getLong("size_bytes"))).list();
    }

    @Transactional(readOnly = true)
    public FileContent download(UUID eventId, UUID fileId) {
        requireRead(eventId);
        return jdbc.sql("""
                select f.original_name, c.contents
                from tep_dinh_kem f join noi_dung_tep_de_xuat c on c.attachment_id = f.id
                where f.id = :file and f.target_type = 'SU_KIEN' and f.target_id = :event and f.archived_at is null
                """).param("event", eventId.toString()).param("file", fileId.toString())
                .query((row, index) -> new FileContent(row.getString("original_name"), row.getBytes("contents")))
                .optional().orElseThrow(() -> ApiException.notFound("Tep de xuat", fileId));
    }

    @Transactional
    public FileInfo upload(UUID eventId, MultipartFile file) throws IOException {
        UUID actor = requireOwnDraft(eventId);
        if (file.isEmpty() || file.getSize() > maxBytes) {
            throw ApiException.validation("file", "Tep rong hoac vuot gioi han " + maxBytes + " byte");
        }
        String name = safeName(file.getOriginalFilename());
        byte[] bytes = file.getBytes();
        String media = mediaType(name, bytes);
        long fileCount = jdbc.sql("""
                select count(*) from tep_dinh_kem where target_type = 'SU_KIEN' and target_id = :event and archived_at is null
                """).param("event", eventId.toString()).query(Long.class).single();
        if (fileCount >= maxFiles) throw ApiException.validation("file", "Toi da " + maxFiles + " tep cho moi de xuat");
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                insert into tep_dinh_kem (id, target_type, target_id, original_name, media_type, size_bytes,
                                         storage_key, sha256, uploaded_by, created_by)
                values (:id, 'SU_KIEN', :event, :name, :media, :size, :storage, :digest, :actor, :actor)
                """).param("id", id.toString()).param("event", eventId.toString()).param("name", name)
                .param("media", media).param("size", bytes.length).param("storage", "event-file/" + id)
                .param("digest", sha256(bytes)).param("actor", actor.toString()).update();
        jdbc.sql("insert into noi_dung_tep_de_xuat (attachment_id, contents) values (:id, :contents)")
                .param("id", id.toString()).param("contents", bytes).update();
        return new FileInfo(id, name, bytes.length);
    }

    @Transactional
    public UUID remove(UUID eventId, UUID fileId) {
        requireOwnDraft(eventId);
        int updated = jdbc.sql("""
                update tep_dinh_kem set archived_at = utc_timestamp(6)
                where id = :file and target_type = 'SU_KIEN' and target_id = :event and archived_at is null
                """).param("file", fileId.toString()).param("event", eventId.toString()).update();
        if (updated == 0) throw ApiException.notFound("Tep de xuat", fileId);
        jdbc.sql("delete from noi_dung_tep_de_xuat where attachment_id = :file").param("file", fileId.toString()).update();
        return fileId;
    }

    private UUID requireOwnDraft(UUID eventId) {
        var event = events.findWithLockById(eventId).orElseThrow(() -> ApiException.notFound("Su kien", eventId));
        EventAccess.requireInScope(event, access);
        UUID actor = access.currentActorId().orElseThrow(() -> ApiException.forbidden("Khong xac dinh duoc nguoi tai tep"));
        if (event.getStatus() != EventStatus.DRAFT || !actor.equals(event.getProposedBy())) {
            throw ApiException.forbidden("Chi nguoi lap duoc cap nhat tep cua ban nhap");
        }
        return actor;
    }

    private void requireRead(UUID eventId) {
        var event = events.findById(eventId).orElseThrow(() -> ApiException.notFound("Su kien", eventId));
        EventAccess.requireInScope(event, access);
        if (event.getStatus() == EventStatus.DRAFT && access.currentActorId().filter(id -> id.equals(event.getProposedBy())).isEmpty()) {
            throw ApiException.forbidden("Chi nguoi lap duoc xem tep cua ban nhap");
        }
    }

    private static String safeName(String value) {
        String path = value == null ? "" : value.replace('\\', '/');
        String name = path.substring(path.lastIndexOf('/') + 1).replaceAll("\\p{Cntrl}", "").strip();
        if (name.isEmpty() || name.length() > 300) throw ApiException.validation("file", "Ten tep khong hop le");
        return name;
    }

    private static String mediaType(String name, byte[] bytes) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf") && startsWith(bytes, 0x25, 0x50, 0x44, 0x46, 0x2d)) return "application/pdf";
        if (lower.endsWith(".png") && startsWith(bytes, 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a)) return "image/png";
        if ((lower.endsWith(".jpg") || lower.endsWith(".jpeg")) && startsWith(bytes, 0xff, 0xd8, 0xff)) return "image/jpeg";
        if (lower.endsWith(".docx") && startsWith(bytes, 0x50, 0x4b, 0x03, 0x04)) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        }
        throw ApiException.validation("file", "Dinh dang tep phai la PDF, DOCX, PNG hoac JPEG");
    }

    private static boolean startsWith(byte[] bytes, int... prefix) {
        if (bytes.length < prefix.length) return false;
        for (int index = 0; index < prefix.length; index++) {
            if (Byte.toUnsignedInt(bytes[index]) != prefix[index]) return false;
        }
        return true;
    }

    private static String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException(unavailable);
        }
    }
}
