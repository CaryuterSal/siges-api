package dev.spiffocode.sigesapi;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@IntegrationTestClass
public abstract class FlushedIntegrationTest {
    @Autowired
    StringRedisTemplate redis;

    @BeforeEach
    void cleanRedis() {
        Assertions.assertNotNull(redis.getConnectionFactory());
        redis.getConnectionFactory()
                .getConnection().serverCommands().flushDb();
    }

    @Test
    public void contextLoads(){
    }
}
