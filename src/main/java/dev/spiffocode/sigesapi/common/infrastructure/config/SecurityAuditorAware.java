package dev.spiffocode.sigesapi.common.infrastructure.config;// Source - https://stackoverflow.com/a/49814248
import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class SecurityAuditorAware implements AuditorAware<@NonNull String> {

    @Override
    public @NonNull Optional<String> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        return Optional.of(Objects.requireNonNull(authentication.getPrincipal()).toString());
    }
}
