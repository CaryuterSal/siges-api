package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;

import java.time.LocalDateTime;

public record SpaceFilter(String searchQuery, ReservableStatus statusFilter, Long buildingIdFilter,
                          Boolean studentsAvailableFilter, Long spaceTypeIdFilter, ActiveFilter activeFilter,
                          Integer capacityAtLeastFilter, LocalDateTime requestStartFilter,
                          LocalDateTime requestEndFilter) {
}