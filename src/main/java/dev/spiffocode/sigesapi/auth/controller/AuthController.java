package dev.spiffocode.sigesapi.auth.controller;

import dev.spiffocode.sigesapi.auth.controller.dto.*;
import dev.spiffocode.sigesapi.auth.service.JwtAuthService;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
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
@OpenAPIDefinition(
        externalDocs = @ExternalDocumentation(url = "https://www.jwt.io/", description = "Uso de JWT")
)
public class AuthController {

    private final JwtAuthService authService;

    @PostMapping("/login")
    public AuthenticatedResponse login(@RequestBody @Valid LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@RequestBody @Valid RefreshRequest req){
        return authService.refresh(req);
    }

    @PostMapping("/logout")
    public ResponseEntity<@NonNull Void> logout(
            @RequestHeader("Authorization") String header,
            @RequestBody @Valid  LogoutRequest logoutRequest){

        String access = header.substring(7);
        authService.logout(access,  logoutRequest);

        return ResponseEntity.noContent().build();
    }

}
