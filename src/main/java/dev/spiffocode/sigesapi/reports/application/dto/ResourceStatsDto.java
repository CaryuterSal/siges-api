package dev.spiffocode.sigesapi.reports.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ResourceStatsDto {
    @Schema(example = "1")
    Long reservableId;
    @Schema(example = "Laboratorio 1")
    String resourceName;
    @Schema(example = "AVAILABLE")
    String resourceStatus;
    @Schema(example = "SPACE")
    String resourceType;
    @Schema(example = "120")
    Long totalReservations;
    @Schema(example = "15")
    Long reservationsThisMonth;
    @Schema(example = "75.5")
    Double occupancyRate;
    @Schema(example = "2.3")
    Double avgDaysBetweenReservations;
}
