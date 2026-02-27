package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record SpaceTypeUpdateDto(

        @Schema(description = "short name, not necessarily unique, that identifies the type of space", example = "Aula")
        @Size(max = 45)
        @NotBlank
        String name,

        @Schema(description = "Short description with details about the type of space", example = "Salón de clases útil para presentaciones y pequeñas conferencias")
        @Size(max = 400)
        @NotBlank
        String description
) {
}
