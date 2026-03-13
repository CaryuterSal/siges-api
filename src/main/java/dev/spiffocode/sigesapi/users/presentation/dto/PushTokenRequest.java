package dev.spiffocode.sigesapi.users.presentation.dto;

import dev.spiffocode.sigesapi.notifications.domain.model.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PushTokenRequest(
        @NotBlank String token,
        String deviceId,
        @NotNull Platform platform) {
}
