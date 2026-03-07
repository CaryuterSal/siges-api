package dev.spiffocode.sigesapi.reservations.presentation;

import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ChangeReservationStatusRequest(

        @NotNull
        @Schema(description = "The intended status")
        Status status
) {
}
