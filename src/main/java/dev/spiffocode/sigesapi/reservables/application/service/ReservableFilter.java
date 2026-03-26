package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import lombok.Builder;
import lombok.Data;
import lombok.With;

import java.time.LocalDateTime;

@Data
@Builder
public class ReservableFilter {
    private String searchQuery;
    private ReservableStatus status;
    private Long buildingIdFilter;
    private Boolean studentsAvailableFilter;
    private LocalDateTime requestStartFilter;
    private LocalDateTime requestEndFilter;
    @Builder.Default
    @With
    private ShowModeFilter showModeFilter = ShowModeFilter.ACTIVE;
}
