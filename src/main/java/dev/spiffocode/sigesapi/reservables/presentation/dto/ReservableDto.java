package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.time.LocalDateTime;

@AllArgsConstructor
@Value
@NonFinal
public class ReservableDto {
    long id;
    @Schema(examples = {"AVAILABLE", "MAINTENANCE", "LOANED"}, example = "AVAILABLE")
    String status;
    @Schema(example = "Cable HDMI de 10 Mts")
    String description;
    boolean availableForStudents;
    BuildingDto buildingDto;
    LocalDateTime createdAt;
    @Schema(example = "admin@example.com")
    String createdBy;
}
