package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Builder
@Jacksonized
public record AvailabilitySlotDto(


    @Schema(description = "ID of this slot/group of availability")
    @Positive
    Long id,

    @Schema(description = "ID of the reservable that is described by this slot")
    @Positive
    Long reservableId,

    @Schema(description = "Date from which this availability statement is valid. If NULL or not specified, it is interpreted as immediate validity.")
    @FutureOrPresent
    LocalDate dateFrom,

    @Schema(description = "Date until which this availability statement is valid. If NULL or not specified, it is interpreted as valid indefinitely.")
    @FutureOrPresent
    LocalDate dateTo,

    @Schema(description = "Availability start time")
    @NotNull
    LocalTime startTime,

    @Schema(description = "Availability end time")
    @NotNull
    LocalTime endTime,

    @Schema(description = "Days of the week this availability statement is valid")
    @ArraySchema(uniqueItems = true, minItems = 1, maxItems = 7)
    Set<DayOfWeek> daysOfWeek
){}
