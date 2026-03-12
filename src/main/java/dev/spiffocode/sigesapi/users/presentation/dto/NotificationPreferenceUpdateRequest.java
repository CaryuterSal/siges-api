package dev.spiffocode.sigesapi.users.presentation.dto;

import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import jakarta.validation.constraints.NotNull;

public record NotificationPreferenceUpdateRequest(
                @NotNull Type type,
                boolean emailEnabled,
                boolean inAppEnabled) {
}
