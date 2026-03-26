package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import lombok.Builder;
import lombok.With;

import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record EquipmentFilter(String searchQuery, ReservableStatus statusFilter, Long buildingIdFilter,
                              Boolean studentsAvailableFilter, Long spaceIdFilter, Long equipmentTypeIdFilter, @With ShowModeFilter showModeFilter,
                              LocalDateTime requestStartFilter, LocalDateTime requestEndFilter) {
}