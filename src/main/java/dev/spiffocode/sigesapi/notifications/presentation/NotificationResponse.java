package dev.spiffocode.sigesapi.notifications.presentation;

import dev.spiffocode.sigesapi.notifications.domain.model.Type;

public record NotificationResponse (
        Long id,
        String title,
        String message,
        Type type
){}
