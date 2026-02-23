package dev.spiffocode.sigesapi.reservables.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

public record SpaceTypeDto(
        long id,
        @Schema(example = "Aula")
        String name,
        @Schema(example = "Salón de clases útil para presentaciones y pequeñas conferencias")
        String description
) {
}
