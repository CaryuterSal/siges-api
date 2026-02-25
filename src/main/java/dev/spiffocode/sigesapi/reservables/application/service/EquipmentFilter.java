package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EquipmentFilter(String searchQuery, ReservableStatus statusFilter, Long buildingIdFilter,
                              Boolean studentsAvailableFilter, Long spaceIdFilter, ActiveFilter activeFilter,
                              LocalDateTime requestStartFilter, LocalDateTime requestEndFilter) {
}