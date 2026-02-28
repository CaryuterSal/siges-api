package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.time.Duration;


@SuperBuilder
@Jacksonized
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUpdateDto extends ReservableUpdateDto{

    @Schema(description = "ID of the space type/category to be registered")
    @NotNull
    @Positive
    Long spaceTypeId;

    @Schema(description = "Time period for which the space must be booked in advance. In case the space requires booking in advance")
    Duration bookInAdvanceDuration;

    @Schema(description = "Maximum person capacity of the space")
    @NotNull
    Integer capacity;
}
