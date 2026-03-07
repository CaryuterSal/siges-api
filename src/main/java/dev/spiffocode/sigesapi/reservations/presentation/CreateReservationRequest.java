package dev.spiffocode.sigesapi.reservations.presentation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.spiffocode.sigesapi.reservations.domain.model.GroupingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Jacksonized
public record CreateReservationRequest(
        @NotNull
        @Positive
        @Schema(description = "ID of the reservable this reservation is issued to")
        Long reservableId,

        @FutureOrPresent
        @NotNull
        @Schema(description = "Date this reservation is intended to")
        LocalDate date,

        @NotNull
        @Schema(description = "Start time the reservation is intended to in the desired date")
        LocalTime startTime,

        @NotNull
        @Schema(description = "Start time the reservation is intended to in the desired date")
        LocalTime endTime,

        @NotNull
        @Schema(description = "Whether the reservation is meant for only one person (SINGLE) or a group of them (GROUP)")
        GroupingType type,

        @Schema(
                description = "Number of companions for the reservation. Only required for group reservations.")
        Integer companions
){
    @AssertTrue(message = "companions is required and must be positive when type is GROUP")
    @JsonIgnore
    public boolean isCompanionsValid() {
        if (type == GroupingType.GROUP) {
            return companions != null && companions > 0;
        }
        return true;
    }
}
