package dev.spiffocode.sigesapi.reservations.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record PublishNoteRequest (
        @NotNull
        @Positive
        @Schema(description = "ID of the reservation the note is gonna be added to")
        Long reservationId,

        @NotBlank
        @Schema(description = "Body of the note")
        String comment
){}
