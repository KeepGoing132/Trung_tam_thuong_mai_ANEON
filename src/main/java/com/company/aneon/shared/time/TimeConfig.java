package com.company.aneon.shared.time;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clock inject duoc theo rule 01; nghiep vu dung zone cau hinh
 * (Asia/Ho_Chi_Minh), thoi diem luu UTC.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock(@Value("${aneon.business-time-zone:Asia/Ho_Chi_Minh}") String zone) {
        Clock clock = Clock.system(ZoneId.of(zone));
        com.company.aneon.shared.persistence.AuditListener.TimeHolder.setClock(clock);
        return clock;
    }

    @Bean
    public ZoneId businessZone(@Value("${aneon.business-time-zone:Asia/Ho_Chi_Minh}") String zone) {
        return ZoneId.of(zone);
    }
}
