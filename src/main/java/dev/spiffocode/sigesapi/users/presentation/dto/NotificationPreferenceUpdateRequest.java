package dev.spiffocode.sigesapi.users.presentation.dto;

import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record NotificationPreferenceUpdateRequest(
                @NotNull
                @Schema(description = "Specific type of notification to edit")
                Type type,
                @Schema(description = "Whether email notifications are enabled")
                boolean emailEnabled,
                @Schema(description = "Whether in-app and push notifications are enabled")
                boolean inAppEnabled) {
}
