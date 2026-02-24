package dev.spiffocode.sigesapi;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

@TestConfiguration
public class FixedClockConfig {


    public static Clock delegate;

    public static void reset(){
        delegate = Clock.fixed(
                Instant.parse("2026-01-01T00:00:00Z"),
                ZoneOffset.UTC
        );
    }

    static {
        reset();
    }

    @Bean(name = "test_clock")
    @Primary
    Clock clock() {
        return new Clock() {
            @Override public ZoneId getZone() { return delegate.getZone(); }
            @Override public Clock withZone(ZoneId zone) { return delegate.withZone(zone); }
            @Override public Instant instant() { return delegate.instant(); }
        };
    }
}
