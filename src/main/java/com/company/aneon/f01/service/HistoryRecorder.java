package com.company.aneon.f01.service;

import java.time.Clock;
import java.util.UUID;

import com.company.aneon.f01.constant.HistoryAction;
import com.company.aneon.f01.entity.LichSuMatBangVaCuaHang;
import com.company.aneon.f01.repository.LichSuMatBangVaCuaHangRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Ghi lich su cung transaction voi thay doi nghiep vu (rule f01).
 * Lich su chi them; loi serialize khong duoc nuot - rollback ca giao dich.
 */
@Component
public class HistoryRecorder {

    public record Change(String action, Object oldValue, Object newValue, String reason) {
    }

    public record Snapshot(String code, String name, UUID zoneId, java.math.BigDecimal totalArea,
                           String type, String status, UUID locationId, String description) {
    }

    private final LichSuMatBangVaCuaHangRepository historyRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public HistoryRecorder(LichSuMatBangVaCuaHangRepository historyRepository,
                           ObjectMapper objectMapper, Clock clock) {
        this.historyRepository = historyRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String targetType, UUID targetId, String action,
                       Object oldValueJson, Object newValueJson, UUID changedBy, String reason) {
        LichSuMatBangVaCuaHang entry = new LichSuMatBangVaCuaHang();
        entry.setId(UUID.randomUUID());
        entry.setTargetType(targetType);
        entry.setTargetId(targetId);
        entry.setAction(action);
        entry.setOldValueJson(writeJson(oldValueJson));
        entry.setNewValueJson(writeJson(newValueJson));
        entry.setChangedBy(changedBy);
        entry.setReason(reason);
        entry.setCreatedAt(java.time.Instant.now(clock));
        entry.setCreatedBy(changedBy);
        historyRepository.save(entry);
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            // Khong nuot loi: lich su phai dung voi thay doi, rollback neu khong ghi duoc
            throw new IllegalStateException("Khong serialize duoc gia tri lich su", ex);
        }
    }
}
