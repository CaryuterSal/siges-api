package dev.spiffocode.sigesapi.reservables.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

@Value
public class SpaceTypeRegisterDto{

    @Schema(description = "short name, not necessarily unique, that identifies the type of space", example = "Aula")
    @Length(max = 45)
    @NotBlank
    String name;

    @Schema(description = "Short description with details about the type of space", example = "Salón de clases útil para presentaciones y pequeñas conferencias")
    @Length(max = 400)
    @NotBlank
    String description;
}
