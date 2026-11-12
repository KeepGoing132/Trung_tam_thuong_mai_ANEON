package com.company.aneon.shared.auth;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;

import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.error.ErrorCode;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
class AuthRateLimit {
    private final JdbcClient jdbc;
    private final Clock clock;

    AuthRateLimit(JdbcClient jdbc, Clock clock) { this.jdbc = jdbc; this.clock = clock; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void attempt(String subject, int maximum) {
        String bucket = AuthService.hash(subject);
        Instant now = clock.instant();
        jdbc.sql("""
                insert into gioi_han_dang_nhap (bucket, attempts, expires_at) values (:bucket, 1, :expiry)
                on duplicate key update attempts = if(expires_at <= :now, 1, attempts + 1),
                    expires_at = if(expires_at <= :now, :expiry, expires_at)
                """).param("bucket", bucket).param("now", Timestamp.from(now))
                .param("expiry", Timestamp.from(now.plusSeconds(900))).update();
        if (jdbc.sql("select attempts from gioi_han_dang_nhap where bucket = :bucket")
                .param("bucket", bucket).query(Integer.class).single() > maximum) {
            throw new ApiException(ErrorCode.RATE_LIMITED, "Qua nhieu lan thu. Vui long doi truoc khi thu lai.");
        }
    }

    void clear(String subject) {
        jdbc.sql("delete from gioi_han_dang_nhap where bucket = :bucket").param("bucket", AuthService.hash(subject)).update();
    }
}
