package dev.spiffocode.sigesapi.users.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record RegNumberUpdateRequest(
        //TODO: Validate format with official staff
        @Schema(
                description = "New enrollment number of the student",
                example = "20243ds158",
                examples = {
                        "20253ds158",
                        "20241asd002"
                })
        @NotBlank
        @Pattern(regexp = "^(19|20)\\d{2}[0-9][a-zA-Z]{2,4}\\d{3}$", message = "Invalid registration number. Must be in UTEZ format")
        String registrationNumber
) {
}
