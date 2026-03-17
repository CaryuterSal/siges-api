package dev.spiffocode.sigesapi;

import com.redis.testcontainers.RedisContainer;
import dev.spiffocode.sigesapi.common.infrastructure.config.S3Properties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.io.IOException;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> mysqlContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ServiceConnection
    public RedisContainer redisContainer() {
        return new RedisContainer(DockerImageName.parse("redis:6.2.6"));
    }

    @Bean
    public LocalStackContainer localStackContainer(
            ConfigurableApplicationContext applicationContext
    ) {
        return new LocalStackContainer(
                DockerImageName.parse("localstack/localstack:3"))
                .withServices("s3");
    }

    @Bean
    public S3Client s3Client(LocalStackContainer localstack, S3Properties s3Properties) throws IOException, InterruptedException {
        S3Client client = S3Client.builder()
                .endpointOverride(localstack.getEndpoint())
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                        )
                )
                .region(Region.of(localstack.getRegion()))
                .build();

        CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                .bucket(s3Properties.getBucketName())
                .build();
        client.createBucket(createBucketRequest);

        return client;
    }
}
