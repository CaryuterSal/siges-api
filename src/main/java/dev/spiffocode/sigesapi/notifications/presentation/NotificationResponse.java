package dev.spiffocode.sigesapi.notifications.presentation;

import dev.spiffocode.sigesapi.notifications.domain.model.ReadStatus;
import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Builder
@Jacksonized
public record NotificationResponse (
        Long id,
        String title,
        String message,
        ReadStatus readStatus,
        Type type,
        LocalDateTime sentAt,
        ReservationSummaryResponse reservation
){}
