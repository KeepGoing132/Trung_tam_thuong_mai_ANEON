package com.company.aneon.shared.persistence;

import java.time.Clock;
import java.time.Instant;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * Ghi timestamp tu Clock he thong da cau hinh (instant UTC).
 */
public class AuditListener {

    @PrePersist
    public void prePersist(Object entity) {
        if (entity instanceof Auditable auditable && auditable.getCreatedAt() == null) {
            auditable.setCreatedAtInstant(Instant.now(TimeHolder.clock()));
        }
        touchUpdate(entity);
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        touchUpdate(entity);
    }

    private void touchUpdate(Object entity) {
        if (entity instanceof Auditable auditable) {
            auditable.setUpdatedAtInstant(Instant.now(TimeHolder.clock()));
        }
    }

    /** Holder Clock tao mot lan khi khoi dong (TimeConfig). */
    public static final class TimeHolder {
        private static volatile Clock clock = Clock.systemUTC();

        private TimeHolder() {
        }

        public static Clock clock() {
            return clock;
        }

        public static void setClock(Clock newClock) {
            clock = newClock;
        }
    }
}
