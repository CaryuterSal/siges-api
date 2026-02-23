package dev.spiffocode.sigesapi.reservables.presentation.dto;

import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@SuperBuilder
@Value
@NonFinal
public class ReservableRegisterDto {

    @Schema(description = "resource's status. Available by default")
    ReservableStatus status = ReservableStatus.AVAILABLE;

    @Schema(description = "Short resource description", example = "Cable HDMI de 10 Mts")
    @Length(max = 455)
    @NotBlank
    String description;

    @Schema(description = "Whether this resource can be reserved by students")
    @NotNull
    Boolean studentsAvailable;

    @Schema(description = "ID of the building where this resource is physically located")
    @NotNull
    Long buildingId;

    @ArraySchema(minItems = 1)
    @Schema(description = "Availability recurrent blocks that defines when this resource can be booked")
    @NotNull
    List<AvailabilitySlotRegisterDto> availability;

    @ArraySchema(minItems = 1)
    @Schema(description = "Defines blocks of time when the availability shouldn't be valid and resource can't be booked")
    List<AvailabilityExceptionRegisterDto> exceptions;
}
