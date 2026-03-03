package dev.spiffocode.sigesapi.common.infrastructure.config;// Source - https://stackoverflow.com/a/49814248

import dev.spiffocode.sigesapi.auth.infrastructure.SecurityContextHelper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityAuditorAware implements AuditorAware<@NonNull String> {

    private final SecurityContextHelper securityContextHelper;

    @Override
    public @NonNull Optional<String> getCurrentAuditor() {
        if(!securityContextHelper.isAuthenticated()) {
            return Optional.empty();
        }

        return Optional.of(securityContextHelper.getCurrentUserEmail());
    }
}
