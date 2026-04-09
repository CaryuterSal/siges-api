package dev.spiffocode.sigesapi.reservations.infrastructure.service.impl;

import dev.spiffocode.sigesapi.reservations.domain.model.GroupingType;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.util.List;

@Builder
@Jacksonized
public record ReservationFilterRequest(

        @Schema(description = "Filter by petitioner ID") Long petitionerId,

        @Schema(description = "Filter by petitioner name (partial, case-insensitive)") String petitionerName,

        @Schema(description = "Filter by exact date") LocalDate date,

        @Schema(description = "Filter reservations from this date (inclusive)") LocalDate dateFrom,

        @Schema(description = "Filter reservations until this date (inclusive)") LocalDate dateTo,

        @Schema(description = "Filter by status") List<Status> statuses,

        @Schema(description = "Filter by reservable ID") Long reservableId,

        @Schema(description = "Filter by grouping type") GroupingType type,

        @Schema(description = "Search query (reservable name, building name, or petitioner name)") String q) {
}