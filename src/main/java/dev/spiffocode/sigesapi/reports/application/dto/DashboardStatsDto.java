package dev.spiffocode.sigesapi.reports.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record DashboardStatsDto(@Schema(example = "5") Integer pendingRequests,
                                @Schema(example = "2") Integer pendingRequestsToday,
                                @Schema(example = "15.5") Double pendingRequestsPercentage,
                                @Schema(example = "1") Integer pendingRequestsDiffYesterday,
                                @Schema(example = "10") Integer availableSpaces,
                                @Schema(example = "12") Integer totalSpaces,
                                @Schema(example = "83.3") Double availableSpacesPercentage,
                                @Schema(example = "-1") Integer availableSpacesDiffYesterday,
                                @Schema(example = "8") Integer inUseEquipments,
                                @Schema(example = "20") Integer totalEquipments,
                                @Schema(example = "40.0") Double inUseEquipmentsPercentage,
                                @Schema(example = "2") Integer inUseEquipmentsDiffYesterday,
                                @Schema(example = "15") Integer todayReservations,
                                @Schema(example = "12.5") Double avgDailyReservations30d,
                                @Schema(example = "3") Integer todayReservationsDiffAvg,
                                @Schema(example = "350") Integer reservationsThisMonth) {
}
