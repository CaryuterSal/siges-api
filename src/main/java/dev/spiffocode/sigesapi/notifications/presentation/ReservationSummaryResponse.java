package dev.spiffocode.sigesapi.notifications.presentation;

import dev.spiffocode.sigesapi.reservations.domain.model.GroupingType;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Builder
@Jacksonized
public record ReservationSummaryResponse(
        Long id,
        Status status,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        GroupingType type,
        Integer companions,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt,
        LocalDateTime cancelledAt,
        LocalDateTime finishedAt,
        LocalDateTime createdAt
) {
}
