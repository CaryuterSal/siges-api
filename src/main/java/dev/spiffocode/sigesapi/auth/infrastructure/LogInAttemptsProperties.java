package dev.spiffocode.sigesapi.auth.infrastructure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties("login")
@Getter
@Setter
@ToString
public class LogInAttemptsProperties {

    private Integer maxAttempts;
    private Integer lockMinutes;
}
