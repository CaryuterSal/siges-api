package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder(toBuilder = true)
@Jacksonized
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Value
@NonFinal
public class EquipmentUpdateDto extends ReservableUpdateDto {

    @Schema(description = "Unique equipment ID in the internal inventory", example = "IN0032")
    @NotBlank
    String inventoryNum;

    @Schema(description = "ID of the optional space to which the equipment is related. For example, a projector can be related to a classroom.")
    @Positive
    Long spaceId;

    @Schema(description = "ID of the equipment type to which the equipment belongs.")
    @NotNull
    @Positive
    Long equipmentTypeId;
}
