package dev.spiffocode.sigesapi.users.infrastructure.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Component
@ConfigurationProperties("app.recovery")
@Getter
@Setter
@ToString
@Validated
public class RecoveryProperties{
    @NotBlank String webRedirectUrl;
    @NotBlank String mobileRedirectUrl;   // myapp://reset-password (deep link)
    @NotNull Duration tokenExpiration;   // PT15M = 15 minutos
}