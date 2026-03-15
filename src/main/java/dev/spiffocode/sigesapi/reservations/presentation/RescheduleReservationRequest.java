package dev.spiffocode.sigesapi.reservations.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Jacksonized
public record RescheduleReservationRequest (

    @FutureOrPresent
    @NotNull
    @Schema(description = "Date this reservation is rescheduled to")
    LocalDate date,

    @NotNull
    @Schema(description = "Start time the reservation is intended to in the desired date")
    LocalTime startTime,

    @NotNull
    @Schema(description = "Start time the reservation is intended to in the desired date")
    LocalTime endTime
){}
