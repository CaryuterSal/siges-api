package dev.spiffocode.sigesapi.reservables.application.service;

import lombok.Builder;

@Builder
public record SpaceAssetFilter(
        String searchQuery,
        Long buildingIdFilter,
        Long spaceIdFilter,
        Long equipmentTypeIdFilter,
        ShowModeFilter showModeFilter
) {
}
