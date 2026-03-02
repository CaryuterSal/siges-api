package dev.spiffocode.sigesapi.common.infrastructure.web;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("security.jwt")
@Getter
@Setter
@ToString
public class JwtProperties {
    private String secret;
    private Long refreshExpiration;
    private Long accessExpiration;


}
