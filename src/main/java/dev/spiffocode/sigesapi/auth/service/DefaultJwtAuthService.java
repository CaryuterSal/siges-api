package dev.spiffocode.sigesapi.auth.service;

import com.auth0.jwt.exceptions.JWTVerificationException;
import dev.spiffocode.sigesapi.auth.controller.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultJwtAuthService implements JwtAuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService blacklistService;
    private final JwtService jwtService;

    @Override
    public AuthenticatedResponse login(LoginRequest req) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.identifier(), req.password())
        );

        UserDetails user = (UserDetails) auth.getPrincipal();

        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String accessToken = jwtService.generateAccessToken(user.getUsername(), roles);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        String role = user.getAuthorities().stream()
                .filter(a -> a.getAuthority().startsWith("ROLE_"))
                .map(a -> a.getAuthority().substring(5))
                .findFirst().get();

        return new AuthenticatedResponse(accessToken, refreshToken, role, user.getAuthorities());
    }

    @Override
    public RefreshResponse refresh(RefreshRequest req) {
        if (!jwtService.isRefreshToken(req.refreshToken())) {
            throw new JWTVerificationException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(req.refreshToken());

        var user = userDetailsService.loadUserByUsername(username);

        var roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new RefreshResponse(jwtService.generateAccessToken(username, roles));
    }

    @Override
    public void logout(String accessToken, LogoutRequest logoutRequest) {
        var accessJwt = jwtService.validate(accessToken);
        var refreshJwt = jwtService.validate(logoutRequest.refreshToken());

        blacklistService.blacklist(accessToken, accessJwt.getExpiresAt());
        blacklistService.blacklist(logoutRequest.refreshToken(), refreshJwt.getExpiresAt());
    }
}
