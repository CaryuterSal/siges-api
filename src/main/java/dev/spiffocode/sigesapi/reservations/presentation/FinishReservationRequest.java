package dev.spiffocode.sigesapi.reservations.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record FinishReservationRequest(
        @Schema(description = "Whether the resource was returned late (se entregó con retraso)") Boolean returnedLate) {
}
