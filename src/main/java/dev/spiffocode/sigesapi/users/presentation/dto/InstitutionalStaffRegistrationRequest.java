package dev.spiffocode.sigesapi.users.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@SuperBuilder(toBuilder = true)
@Jacksonized
@Value
@EqualsAndHashCode(callSuper = true)
public class InstitutionalStaffRegistrationRequest extends ApplicantRegistrationRequest{

    //TODO: Validate format with official staff
    @Schema(
            description = "Unique employee number for the institutional staff",
            example = "IN-002"
    )
    @NotBlank
    String employeeNumber;
}
