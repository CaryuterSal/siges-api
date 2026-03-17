package dev.spiffocode.sigesapi.reservables.presentation.dto;

import dev.spiffocode.sigesapi.reservables.domain.model.EquipmentType;
import dev.spiffocode.sigesapi.users.presentation.dto.AdminResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Builder
@Jacksonized
public record SpaceAssetDto(
                Long id,
                @Schema(example = "OLED TV") String name,
                @Schema(example = "Simple TV to use for class purposes") String description,
                @Schema(example = "IN0013") String inventoryNum,
                SpaceSummaryDto space,
                EquipmentType type,
                LocalDateTime createdAt,
                LocalDateTime updatedAt,
                @Schema(example = "admin@example.com") String createdBy,
                LocalDateTime deletedAt) {
}
