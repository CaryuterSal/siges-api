package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder
@Jacksonized
@Value
@EqualsAndHashCode(callSuper = true)
public class EquipmentRegisterDto extends ReservableRegisterDto {

    @Schema(description = "Unique team ID in the internal inventory", example = "IN0032")
    @NotBlank
    String inventoryNum;

    @Schema(description = "ID of the optional space to which the equipment is related. For example, a projector can be related to a classroom.")
    @Positive
    Long spaceId;

}
