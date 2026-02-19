package dev.spiffocode.sigesapi.auth.infrastructure;

import com.auth0.jwt.exceptions.JWTVerificationException;
import dev.spiffocode.sigesapi.auth.application.service.JwtAuthService;
import dev.spiffocode.sigesapi.auth.presentation.*;
import dev.spiffocode.sigesapi.common.infrastructure.exceptions.JwtBlacklistedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BlacklistedJwtService implements JwtAuthService {

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

        assert user != null;
        List<String> roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String accessToken = jwtService.generateAccessToken(user.getUsername(), roles);
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        String role = user.getAuthorities().stream()
                .filter(a -> Objects.requireNonNull(a.getAuthority()).startsWith("ROLE_"))
                .map(a -> a.getAuthority().substring(5))
                .findFirst().orElse("ROLE_USER");

        return new AuthenticatedResponse(accessToken, refreshToken, role, user.getAuthorities());
    }

    @Override
    public RefreshResponse refresh(RefreshRequest req) {
        if (!jwtService.isRefreshToken(req.refreshToken())) {
            throw new JWTVerificationException("Invalid refresh token");
        }

        if(blacklistService.isBlacklisted(jwtService.extractJti(req.refreshToken()))) {
            throw new JwtBlacklistedException("Invalid refresh token");
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

        blacklistService.blacklist(accessJwt.getId(), accessJwt.getExpiresAt());
        blacklistService.blacklist(refreshJwt.getId(), refreshJwt.getExpiresAt());
    }
}
