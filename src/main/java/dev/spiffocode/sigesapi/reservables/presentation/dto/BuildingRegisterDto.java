package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record BuildingRegisterDto(
        @Schema(description = "unique short name for the building", example = "Docencia 1")
        @NotBlank
        String name
) {
}
