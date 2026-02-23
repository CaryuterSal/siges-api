package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.time.Duration;

@SuperBuilder
@Value
@EqualsAndHashCode(callSuper = true)
public class SpaceRegisterDto extends ReservableRegisterDto {

    @Schema(description = "ID of the space type/category to be registered")
    @NotNull
    @Positive
    Long spaceTypeId;

    @Schema(description = "Time period for which the space must be booked in advance. In case the space requires booking in advance")
    Duration bookInAdvanceDuration;
}
