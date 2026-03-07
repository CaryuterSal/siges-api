package dev.spiffocode.sigesapi.notifications.application.service;

import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import lombok.Builder;

@Builder
public record SendNotificationCommand(
        Type type,
        String title,
        String message
){
}
