package dev.spiffocode.sigesapi.users.presentation.dto;

import dev.spiffocode.sigesapi.users.infrastructure.validator.PhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@SuperBuilder(toBuilder = true)
@Value
@NonFinal
public sealed abstract class UserRegistrationRequest permits AdminRegistrationRequest, ApplicantRegistrationRequest {

    @Schema(
            description = "Email of the user to register. Must have 'utez.edu.mx' domain",
            example = "student1@utez.edu.mx")
    @NotBlank
    @Email
    @Pattern(regexp = "^[a-zA-Z0-9._%+\\-]+@utez\\.edu\\.mx$", message = "Email must have 'utez.edu.mx' domain")
    String email;

    @Schema(
            description = "Phone number of the user. Accepts multiple formats, normalized to E.164 before persisting",
            example = "+525512345678",
            examples = {
                    "+525512345678",
                    "5512345678",
                    "55 1234 5678",
                    "55-1234-5678",
                    "(55) 1234-5678",
            }
    )
    @NotBlank
    @PhoneNumber
    String phoneNumber;

    @Schema(
            description = "First name of the user",
            example = "Juan Rodrigo"
    )
    @NotBlank
    String firstName;

    @Schema(
            description = "Last name of the user",
            example = "García López"
    )
    @NotBlank
    String lastName;

    @Schema(
            description = "Birth date of the user. Must be a past date",
            example = "2000-05-21"
    )
    @NotNull
    @Past
    LocalDate birthDate;

}
