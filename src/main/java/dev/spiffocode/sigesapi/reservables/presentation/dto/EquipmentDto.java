package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;


@Setter
@SuperBuilder
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@Value
public class EquipmentDto extends ReservableDto {
    SpaceDto spaceAttached;
    @Schema(example = "IN0013")
    String inventoryIdNum;
}
