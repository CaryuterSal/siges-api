package dev.spiffocode.sigesapi.users.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record RequestAccountRecovery(
        @Schema(
                description = "Email of the account to request password recovery. Must have 'utez.edu.mx' domain",
                example = "student1@utez.edu.mx")
        @NotBlank
        @Email
        @Pattern(regexp = "^[a-zA-Z0-9._%+\\-]+@utez\\.edu\\.mx$", message = "Email must have 'utez.edu.mx' domain")
        String email
) {
}
