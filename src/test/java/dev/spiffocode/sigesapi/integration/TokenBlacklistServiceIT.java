package dev.spiffocode.sigesapi.integration;

import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.service.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Duration;
import java.util.Date;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationTestClass
class TokenBlacklistServiceIT {

    @Autowired
    TokenBlacklistService service;
    @Autowired
    private Clock clock;

    @Test
    void blacklist_expires() {

        Date expires = new Date(clock.millis() + 1000);

        assertFalse(service.isBlacklisted("abc"));

        service.blacklist("abc", expires);

        assertTrue(service.isBlacklisted("abc"));

        await()
                .atLeast(Duration.ofSeconds(1))
                .atMost(Duration.ofSeconds(2))
                .until(() -> !service.isBlacklisted("abc"));
    }
}
