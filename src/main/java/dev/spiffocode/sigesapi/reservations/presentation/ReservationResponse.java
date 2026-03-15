package dev.spiffocode.sigesapi.reservations.presentation;

import dev.spiffocode.sigesapi.reservables.presentation.dto.ReservableDto;
import dev.spiffocode.sigesapi.reservations.domain.model.GroupingType;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.users.presentation.dto.UserResponse;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Builder
@Jacksonized
public record ReservationResponse(
        Long id,
        UserResponse petitioner,
        ReservableDto reservable,
        List<NoteItem> notes,
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
