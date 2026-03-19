package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipmentTypeUpdateDto(
                @Schema(description = "Short name that identifies the type of equipment", example = "Proyector") @Size(max = 45) @NotBlank String name,

                @Schema(description = "Short description with details about the type of equipment", example = "Equipo de proyección visual para presentaciones") @Size(max = 400) String description) {
}
