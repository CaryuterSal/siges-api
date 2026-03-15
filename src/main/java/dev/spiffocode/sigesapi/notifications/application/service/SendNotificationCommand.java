package dev.spiffocode.sigesapi.notifications.application.service;

import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import lombok.Builder;

import java.util.Collections;
import java.util.Map;

@Builder
public record SendNotificationCommand(
        Type type,
        String title,
        String message,
        Long entityId,
        Map<String, String> metadata) {

    public SendNotificationCommand {
        if (metadata == null) {
            metadata = Collections.emptyMap();
        }
    }
}
