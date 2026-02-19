package dev.spiffocode.sigesapi.auth.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.Date;
@RequiredArgsConstructor
@Service
public class TokenBlacklistService {

    private final StringRedisTemplate redis;
    private final Clock clock;

    private static final String PREFIX = "blacklist:";

    public void blacklist(String jti, Date expiresAt) {

        long ttlMillis = expiresAt.getTime() - clock.millis();

        if (ttlMillis > 0) {
            redis.opsForValue().set(
                    PREFIX + jti,
                    "1",
                    Duration.ofMillis(ttlMillis)
            );
        }
    }

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + jti));
    }
}
