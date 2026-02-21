package dev.spiffocode.sigesapi.auth.infrastructure;

import com.auth0.jwt.exceptions.JWTVerificationException;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.domain.exception.AccountTemporarilyLockedException;
import dev.spiffocode.sigesapi.auth.domain.exception.InvalidCredentialsException;
import dev.spiffocode.sigesapi.auth.domain.exception.JwtBlacklistedException;
import dev.spiffocode.sigesapi.auth.domain.model.LogInAttempt;
import dev.spiffocode.sigesapi.auth.domain.model.LogInAttemptsRepository;
import dev.spiffocode.sigesapi.auth.presentation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BlacklistedJwtAuthService implements BearerAuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final LogInAttemptsRepository logInAttemptsRepository;
    private final LogInAttemptsProperties loginProperties;
    private final TokenBlacklistService blacklistService;
    private final JwtService jwtService;
    private final Clock clock;

    @Transactional
    @Override
    public AuthenticatedResponse login(LoginRequest req, String requestIp) {


        LocalDateTime since = LocalDateTime.now(clock)
                .minusMinutes(loginProperties.getLockMinutes());

        long recentFailures = logInAttemptsRepository
                .countRecentFailuresSinceLastSuccess(req.identifier(), requestIp, since);
        if (recentFailures >= loginProperties.getMaxAttempts()) {
            throw new AccountTemporarilyLockedException("Account temporarily locked. Try again in " + loginProperties.getLockMinutes() + " minutes.");
        }

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.identifier(), req.password())
            );

            LogInAttempt attempt = LogInAttempt.builder()
                    .username(req.identifier())
                    .ipAddress(requestIp)
                    .success(true)
                    .timestamp(LocalDateTime.now(clock))
                    .build();
            logInAttemptsRepository.save(attempt);

            return buildResponse(auth);
        } catch (AuthenticationException e) {
            LogInAttempt attempt = LogInAttempt.builder()
                    .username(req.identifier())
                    .ipAddress(requestIp)
                    .success(false)
                    .timestamp(LocalDateTime.now(clock))
                    .build();

            logInAttemptsRepository.save(attempt);
            throw new InvalidCredentialsException(e, loginProperties.getMaxAttempts() - Math.toIntExact(recentFailures) - 1);
        }
    }

    private AuthenticatedResponse buildResponse(Authentication auth) {
        String username = ((UserDetails) Objects.requireNonNull(auth.getPrincipal())).getUsername();
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        String role = roles.stream()
                .filter(r -> r.startsWith("ROLE_"))
                .map(r -> r.substring(5))
                .findFirst().orElse("USER");

        return new AuthenticatedResponse(
                jwtService.generateAccessToken(username, roles),
                jwtService.generateRefreshToken(username),
                role,
                auth.getAuthorities()
        );
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
