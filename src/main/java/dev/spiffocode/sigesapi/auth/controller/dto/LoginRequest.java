package dev.spiffocode.sigesapi.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @Schema(examples = {
                "example@gmail.com",
                "20243ds155",
                "7772683914",
                "275847718"
        },
        example = "example@gmail.com")
        @NotBlank
        String identifier,
        @NotBlank
        String password) {
}
