package dev.spiffocode.sigesapi.users.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record EmpNumberUpdateRequest(
        //TODO: Validate format with official staff
        @Schema(
                description = "Unique employee number for the institutional staff",
                example = "IN-002"
        )
        @NotBlank
        String employeeNumber
) {
}
