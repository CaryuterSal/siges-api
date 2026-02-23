package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record AvailabilityExceptionRegisterDto(

        @Schema(description = "Date from which this availability exception begins")
        @NotNull
        @FutureOrPresent
        LocalDate dateFrom,

        @Schema(description = "Date until which this availability exception begins")
        @NotNull
        @FutureOrPresent
        LocalDate dateTo,

        @Schema(description = "Availability exception start time")
        @NotNull
        LocalTime startTime,

        @Schema(description = "End time of the availability exception")
        @NotNull
        LocalTime endTime,

        @Schema(description = "Short reason or cause for this exception in availability", minLength = 1, maxLength = 255)
        @NotBlank
        String reason


) {
}
