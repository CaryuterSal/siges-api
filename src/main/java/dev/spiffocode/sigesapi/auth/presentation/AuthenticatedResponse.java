package dev.spiffocode.sigesapi.auth.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public record AuthenticatedResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30")
        @NotBlank
        String accessToken,
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.KMUFsIDTnFmyG3nMiGM6H9FNFUROf3wh7SmqJp-QV30")
        @NotBlank
        String refreshToken,
        @Schema(examples = {"STUDENT", "ADMIN", "INSTITUTIONAL_STAFF"}, example = "STUDENT")
        @NotBlank
        String role,

        @Schema(example = "[{\"authority\": \"ROLE_STUDENT\"}]")
        @NotNull
        Collection<? extends GrantedAuthority> claims) {
}
