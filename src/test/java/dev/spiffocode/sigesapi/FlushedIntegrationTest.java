package dev.spiffocode.sigesapi;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Type;
import org.hibernate.annotations.Immutable;
import dev.spiffocode.sigesapi.auth.domain.model.LogInAttempt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@IntegrationTestClass
public abstract class FlushedIntegrationTest {
    @Autowired
    StringRedisTemplate redis;
    @Autowired
    EntityManager em;
    @Autowired
    PlatformTransactionManager transactionManager;

    @BeforeEach
    void cleanRedis() {
        Assertions.assertNotNull(redis.getConnectionFactory());
        redis.getConnectionFactory()
                .getConnection().serverCommands().flushDb();
        new TransactionTemplate(transactionManager).execute(status -> {
            em.createNativeQuery("SET session_replication_role = replica").executeUpdate();
            em
                    .getMetamodel()
                    .getEntities()
                    .stream()
                    .map(Type::getJavaType)
                    .filter(type -> {
                        if (type.getAnnotation(Immutable.class) == null)
                            return true;
                        if (type.equals(LogInAttempt.class))
                            return true;
                        Table table = type.getAnnotation(Table.class);
                        return table == null || !table.name().startsWith("v_");
                    })
                    .map(type -> em.getMetamodel().entity(type).getName())
                    .forEach(name -> em.createQuery("delete from " + name).executeUpdate());
            em.createNativeQuery("SET session_replication_role = DEFAULT").executeUpdate();
            return null;
        });
    }

    @Test
    public void contextLoads() {
    }
}
