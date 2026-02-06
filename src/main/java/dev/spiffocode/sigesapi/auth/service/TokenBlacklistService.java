package dev.spiffocode.sigesapi.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redis;

    private static final String PREFIX = "blacklist:";

    public void blacklist(String token, Date expiresAt) {

        long ttl = (expiresAt.getTime() - System.currentTimeMillis()) / 1000;

        if (ttl > 0) {
            redis.opsForValue().set(PREFIX + token, "1", Duration.ofSeconds(ttl));
        }
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redis.hasKey(PREFIX + token));
    }
}
