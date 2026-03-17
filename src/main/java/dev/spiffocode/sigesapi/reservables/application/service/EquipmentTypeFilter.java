package dev.spiffocode.sigesapi.reservables.application.service;

import lombok.Builder;

@Builder
public record EquipmentTypeFilter(
        ShowModeFilter showModeFilter,
        String query
) {
}
