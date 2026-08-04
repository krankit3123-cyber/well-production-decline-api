package com.bp.decline.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ClockConfig {

    public static final String ZONE_ID = "Asia/Kolkata";
    public static final ZoneId APPLICATION_ZONE_ID = ZoneId.of(ZONE_ID);

    @Bean
    Clock clock() {
        return Clock.system(APPLICATION_ZONE_ID);
    }

    public static Clock applicationClock() {
        return Clock.system(APPLICATION_ZONE_ID);
    }
}
