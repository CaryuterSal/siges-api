package dev.spiffocode.sigesapi.common.infrastructure.config;

import dev.spiffocode.sigesapi.common.infrastructure.web.TimezoneContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return new Clock() {
            @Override
            public ZoneId getZone() {
                return TimezoneContextHolder.getZoneId();
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return Clock.system(zone);
            }

            @Override
            public long millis() {
                return System.currentTimeMillis();
            }

            @Override
            public java.time.Instant instant() {
                return java.time.Instant.now();
            }
        };
    }
}
