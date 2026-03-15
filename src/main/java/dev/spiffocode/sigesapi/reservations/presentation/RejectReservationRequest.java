package dev.spiffocode.sigesapi.reservations.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record RejectReservationRequest(

        @NotBlank
        @Schema(description = "short description of why the reservation is being rejected")
        String reason
) {
}
