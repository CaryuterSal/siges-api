package dev.spiffocode.sigesapi.notifications.application.service;

import dev.spiffocode.sigesapi.notifications.domain.model.ReadStatus;
import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import lombok.Builder;

@Builder
public record NotificationFilter(
        ReadStatus readStatus,
        Type type
) {
}
