package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Builder
@Jacksonized
public record SpaceTypeDto(
        long id,
        @Schema(example = "Aula")
        String name,
        @Schema(example = "Salón de clases útil para presentaciones y pequeñas conferencias")
        String description,
        LocalDateTime deletedAt
) {
}
