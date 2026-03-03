package dev.spiffocode.sigesapi.users.infrastructure.controller;

import dev.spiffocode.sigesapi.common.presentation.ValidationProblem;
import dev.spiffocode.sigesapi.users.application.service.PasswordRecoveryService;
import dev.spiffocode.sigesapi.users.presentation.dto.PasswordUpdateRequest;
import dev.spiffocode.sigesapi.users.presentation.dto.RequestAccountRecovery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(version = "1.0.0")
@RequiredArgsConstructor
@Tag(name = "Password Recovery", description = "Endpoints for whole sequence of password recovery and update")
public class PasswordRecoveryController {

    private final PasswordRecoveryService recoveryService;

    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Recovery email sent if account exists. Always returns 202 to prevent user enumeration"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation problem",
                    content = @Content(schema = @Schema(implementation = ValidationProblem.class)))
    })
    @Operation(summary = "Requests a password recovery flow")
    @PostMapping("/password-recovery/request")
    public ResponseEntity<@NonNull Void> requestRecovery(@RequestBody @Valid RequestAccountRecovery request) {
        recoveryService.requestRecovery(request);
        return ResponseEntity.accepted().build();
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "302",
                    description = "Redirects to frontend reset password page with token as query param",
                    headers = @Header(
                            name = "Location",
                            description = "Frontend URL with token: https://frontend.com/reset-password?token=xxx")),
            @ApiResponse(
                    responseCode = "400",
                    description = "Malformed or missing token",
                    content = @Content(schema = @Schema(implementation = ValidationProblem.class)))
    })
    @Operation(summary = "Redirects email clients to the portal where the password update is done")
    @GetMapping("/password-recovery/redirect")
    public ResponseEntity<@NonNull Void> redirect(
            @RequestParam
            @Schema(description = "Recovery token sent via email", example = "eyJhbGciOiJIUzI1NiJ9...")
            @NotBlank String token) {
        URI frontendUri = recoveryService.redirectRecovery(token);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(frontendUri)
                .build();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password updated successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation problem",
                    content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
            @ApiResponse(
                    responseCode = "410",
                    description = "Token expired or already used",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @Operation(summary = "Updates a password")
    @PatchMapping("/password-recovery/reset")
    public ResponseEntity<@NonNull Void> resetPassword(@RequestBody @Valid PasswordUpdateRequest request) {
        recoveryService.updatePassword(request);
        return ResponseEntity.noContent().build();
    }
}
