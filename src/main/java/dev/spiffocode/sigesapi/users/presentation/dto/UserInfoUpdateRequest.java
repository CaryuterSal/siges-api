package dev.spiffocode.sigesapi.users.presentation.dto;

import dev.spiffocode.sigesapi.users.infrastructure.validator.PhoneNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@Builder(toBuilder = true)
@Jacksonized
public record UserInfoUpdateRequest(

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
        String phoneNumber,

        @Schema(
                description = "First name of the user",
                example = "Juan Rodrigo"
        )
        @NotBlank
        String firstName,

        @Schema(
                description = "Last name of the user",
                example = "García López"
        )
        @NotBlank
        String lastName,

        @Schema(
                description = "Birth date of the user. Must be a past date",
                example = "2000-05-21"
        )
        @NotNull
        @Past
        LocalDate birthDate
) {
}
