package dev.spiffocode.sigesapi.auth.controller.dto;

import dev.spiffocode.sigesapi.auth.service.JwtAuthService;

public record AuthenticatedResponse(String accessToken, String refreshToken) {
}
