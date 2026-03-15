package dev.spiffocode.sigesapi.users.presentation.dto;

import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record NotificationPreferenceResponse(
                Type type,
                boolean emailEnabled,
                boolean inAppEnabled) {
}
