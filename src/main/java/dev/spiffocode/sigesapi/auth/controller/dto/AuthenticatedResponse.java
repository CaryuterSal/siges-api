package dev.spiffocode.sigesapi.auth.controller.dto;

import dev.spiffocode.sigesapi.auth.service.JwtAuthService;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;

public record AuthenticatedResponse(String accessToken, String refreshToken, String role, Collection<? extends GrantedAuthority> claims) {
}
