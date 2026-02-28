package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Builder
@Jacksonized
public record BuildingDto(
        Long id,
        @Schema(example = "Docencia 1")
        String name,
        LocalDateTime deletedAt
) {
}
