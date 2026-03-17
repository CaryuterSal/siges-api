package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Builder
@Jacksonized
public record EquipmentTypeDto(
        long id,
        @Schema(example = "Proyector") String name,
        @Schema(example = "Equipo de proyección visual para presentaciones") String description,
        LocalDateTime deletedAt) {
}
