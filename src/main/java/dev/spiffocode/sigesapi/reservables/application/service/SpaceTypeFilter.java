package dev.spiffocode.sigesapi.reservables.application.service;

import lombok.Builder;

@Builder
public record SpaceTypeFilter(
        String query,
        ShowModeFilter showModeFilter
) {
}
