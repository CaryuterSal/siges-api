package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import lombok.Builder;
import lombok.With;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record SpaceFilter(String searchQuery, ReservableStatus statusFilter, Long buildingIdFilter,
                          Boolean studentsAvailableFilter, Long spaceTypeIdFilter, @With ShowModeFilter showModeFilter,
                          Integer capacityAtLeastFilter, LocalDateTime requestStartFilter,
                          LocalDateTime requestEndFilter) {
}