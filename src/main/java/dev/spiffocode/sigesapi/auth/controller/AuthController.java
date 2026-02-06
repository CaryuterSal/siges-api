package dev.spiffocode.sigesapi.auth.controller;

import dev.spiffocode.sigesapi.auth.controller.dto.*;
import dev.spiffocode.sigesapi.auth.service.JwtAuthService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/auth", version = "1.0.0")
public class AuthController {

    private final JwtAuthService authService;

    @PostMapping("/login")
    public AuthenticatedResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@RequestBody RefreshRequest req){
        return authService.refresh(req);
    }

    @PostMapping("/logout")
    public ResponseEntity<@NonNull Void> logout(
            @RequestHeader("Authorization") String header,
            @RequestBody LogoutRequest logoutRequest){

        String access = header.substring(7);
        authService.logout(access,  logoutRequest);

        return ResponseEntity.noContent().build();
    }

}
