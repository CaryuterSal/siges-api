package dev.spiffocode.sigesapi.auth.controller;

import dev.spiffocode.sigesapi.auth.controller.dto.*;
import dev.spiffocode.sigesapi.auth.service.JwtAuthService;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.FailedApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/auth", version = "1.0.0")
@Tag(name = "auth", description = "Flujo de autenticación y cierre de sesión para usuario")
@SecurityRequirements
@ExternalDocumentation(url = "https://www.jwt.io/", description = "Uso de JWT")
public class AuthController {

    private final JwtAuthService authService;


    @PostMapping("/login")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "auth success", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "auth fails")
    })
    public AuthenticatedResponse login(@RequestBody @Valid LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "refresh success", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "refresh fails")
    })
    public RefreshResponse refresh(@RequestBody @Valid RefreshRequest req){
        return authService.refresh(req);
    }

    @PostMapping("/logout")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "logout success", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "401", description = "auth fails")
    })
    public ResponseEntity<@NonNull Void> logout(
            @RequestHeader("Authorization") String header,
            @RequestBody @Valid  LogoutRequest logoutRequest){

        String access = header.substring(7);
        authService.logout(access,  logoutRequest);

        return ResponseEntity.noContent().build();
    }

}
