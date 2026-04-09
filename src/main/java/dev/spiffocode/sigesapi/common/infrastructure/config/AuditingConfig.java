package dev.spiffocode.sigesapi.common.infrastructure.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
public class AuditingConfig {

    @Bean
    public DateTimeProvider dateTimeProvider(ObjectProvider<Clock> clockProvider) {
        Clock clock = clockProvider.getIfAvailable(() -> Clock.systemDefaultZone());
        return () -> Optional.of(LocalDateTime.now(clock));
    }
}
