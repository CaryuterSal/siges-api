package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record BuildingDto(
        Long id,
        @Schema(example = "Docencia 1")
        String name,
        LocalDateTime deletedAt
) {
}
