package dev.spiffocode.sigesapi.reservations.presentation;

import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.users.presentation.dto.ApplicantResponse;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Builder
@Jacksonized
public record ReservationResponse(
    Long id,
    ApplicantResponse applicant,
    Status status,
    LocalTime startTime,
    LocalTime endTime,
    LocalDate dateFrom,
    LocalDate dateTo,
    LocalDateTime approvedAt,
    LocalDateTime createdAt,
    List<NoteItem> notes,
    boolean isRecurrent,
    List<DayOfWeek> daysRecurrent
){}
