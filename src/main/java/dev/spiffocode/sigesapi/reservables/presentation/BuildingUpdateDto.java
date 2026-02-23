package dev.spiffocode.sigesapi.reservables.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record BuildingUpdateDto(

        @Schema(description = "unique short name for the building", example = "Docencia 1")
        @NotBlank
        String name
) {
}
