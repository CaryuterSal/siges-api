package dev.spiffocode.sigesapi.users.presentation.dto;

import dev.spiffocode.sigesapi.notifications.domain.model.Type;

public record NotificationPreferenceResponse(
                Type type,
                boolean emailEnabled,
                boolean inAppEnabled) {
}
