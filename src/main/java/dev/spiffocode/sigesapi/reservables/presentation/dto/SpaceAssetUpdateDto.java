package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.Length;

@Builder
@Jacksonized
public record SpaceAssetUpdateDto(
        @Schema(description = "Unique Name of the technical space asset")
        @NotBlank
        @Length(max = 200)
        String name,
        @Schema(description = "short description for the technical space asset")
        @NotBlank
        @Length(max = 400)
        String description,
        @Schema(description = "Inventory Number of the technical space asset. It must be unique among equipment and space assets")
        @NotBlank
        String inventoryNum,
        @Schema(description = "ID of the type of equipment this asset is described as")
        @NotNull
        Long typeId
) {
}
