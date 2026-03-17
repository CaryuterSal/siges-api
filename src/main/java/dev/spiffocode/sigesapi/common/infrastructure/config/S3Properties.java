package dev.spiffocode.sigesapi.common.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "aws.s3")
public class S3Properties {
    private String region;
    private String bucketName;
    private String cloudfrontDomain;
}
