package dev.spiffocode.sigesapi.reservables.presentation.dto;

import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Value
@NonFinal
public class ReservableUpdateDto {

    @Schema(description = "resource's status. Available by default")
    ReservableStatus status = ReservableStatus.AVAILABLE;

    @Schema(description = "Short name.Not necessarily unique", example = "Cable HDMI")
    @Size(max = 200)
    @NotBlank
    String name;

    @Schema(description = "Short resource description", example = "Cable HDMI de 10 Mts")
    @Size(max = 455)
    String description;

    @Schema(description = "Whether this resource can be reserved by students")
    @NotNull
    Boolean studentsAvailable;

    @Schema(description = "ID of the building where this resource is physically located")
    @NotNull
    Long buildingId;
}
