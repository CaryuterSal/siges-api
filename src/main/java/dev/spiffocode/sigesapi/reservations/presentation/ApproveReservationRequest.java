package dev.spiffocode.sigesapi.reservations.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ApproveReservationRequest(
        @Schema(description = "optional observation by the admin when approving the reservation") String observation) {
}
