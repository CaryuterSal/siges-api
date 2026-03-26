package dev.spiffocode.sigesapi.reservables.presentation.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@SuperBuilder
@Jacksonized
@AllArgsConstructor
@Value
@NonFinal
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "resourceType", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SpaceDto.class, name = "SPACE"),
        @JsonSubTypes.Type(value = EquipmentDto.class, name = "EQUIPMENT")
})
public class ReservableDto {
    @Schema(example = "SPACE", examples = {"SPACE", "EQUIPMENT"})
    String resourceType;
    long id;
    @Schema(example = "Cable HDMI")
    String name;
    @Schema(examples = { "AVAILABLE", "MAINTENANCE", "LOANED" }, example = "AVAILABLE")
    String status;
    @Schema(example = "Cable HDMI de 10 Mts")
    String description;
    boolean availableForStudents;
    BuildingDto building;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    @Schema(example = "admin@example.com")
    String createdBy;
    LocalDateTime deletedAt;
    List<AvailabilityExceptionDto> availabilityExceptions;
    List<AvailabilitySlotDto> availabilitySlots;
}
