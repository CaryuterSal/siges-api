package dev.spiffocode.sigesapi.users.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder(toBuilder = true)
@Jacksonized
public record PasswordRecoveryUpdateRequest(
        @Schema(
                description = "Recovery token received via email",
                example = "eyJhbGciOiJIUzI1NiJ9..."
        )
        @NotBlank
        String token,

        @Schema(
                description = "New password. Must contain at least one uppercase, one lowercase, one number and one special character",
                example = "NewPass123!"
        )
        @NotBlank
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$",
                message = "Password must be at least 8 characters and contain uppercase, lowercase, number and special character"
        )
                String newPassword
) {
}
