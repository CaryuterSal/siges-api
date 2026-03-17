package dev.spiffocode.sigesapi.reservables.presentation.dto;

import dev.spiffocode.sigesapi.reservables.domain.model.EquipmentType;
import dev.spiffocode.sigesapi.users.presentation.dto.AdminResponse;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Builder
@Jacksonized
public record SpaceAssetDto(
        Long id,
        String name,
        String description,
        String inventoryNum,
        SpaceSummaryDto space,
        EquipmentType type,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        AdminResponse createdBy,
        LocalDateTime deletedAt
) {
}
