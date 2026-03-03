package dev.spiffocode.sigesapi.auth.infrastructure.service.impl;

import com.auth0.jwt.exceptions.JWTVerificationException;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.application.service.CustomUserDetails;
import dev.spiffocode.sigesapi.auth.domain.exception.AccountTemporarilyLockedException;
import dev.spiffocode.sigesapi.auth.domain.exception.InvalidCredentialsException;
import dev.spiffocode.sigesapi.auth.domain.exception.JwtBlacklistedException;
import dev.spiffocode.sigesapi.auth.domain.model.LogInAttemptsRepository;
import dev.spiffocode.sigesapi.auth.infrastructure.JwtService;
import dev.spiffocode.sigesapi.auth.infrastructure.LogInAttemptsProperties;
import dev.spiffocode.sigesapi.auth.infrastructure.TokenBlacklistService;
import dev.spiffocode.sigesapi.auth.presentation.dto.*;
import dev.spiffocode.sigesapi.users.domain.model.User;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
    private final UserRepository userRepository;
    private final LogInAttemptsRepository logInAttemptsRepository;
    private final LogInAttemptsProperties loginProperties;
    private final TokenBlacklistService blacklistService;
    private final JwtService jwtService;
    private final Clock clock;
    private final LoginAttemptRecorder loginAttemptRecorder;

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
            loginAttemptRecorder.recordSuccess(req.identifier(), requestIp);
            User user = userRepository.findByIdentifier(((UserDetails) Objects.requireNonNull(auth.getPrincipal())).getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));
            user.recordLogin(clock);
            userRepository.save(user);

            return buildResponse(auth);
        } catch (AuthenticationException e) {
            loginAttemptRecorder.recordFailure(req.identifier(), requestIp);
            throw new InvalidCredentialsException(e, loginProperties.getMaxAttempts() - Math.toIntExact(recentFailures) - 1);
        }
    }

    private AuthenticatedResponse buildResponse(Authentication auth) {
        CustomUserDetails userDetails = (CustomUserDetails) Objects.requireNonNull(auth.getPrincipal());
        String username = userDetails.getUsername();
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        String role = roles.stream()
                .filter(r -> r.startsWith("ROLE_"))
                .map(r -> r.substring(5))
                .findFirst().orElse("USER");

        Integer tokenVersion = userDetails.getTokenVersion();

        return new AuthenticatedResponse(
                jwtService.generateAccessToken(username, roles, tokenVersion),
                jwtService.generateRefreshToken(username, tokenVersion),
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

        var user = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

        if(!Objects.equals(user.getTokenVersion(), jwtService.extractTokenVersion(req.refreshToken()))) {
            throw new JwtBlacklistedException("Token version is not valid anymore. User updated sensitive data");
        }

        var roles = user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new RefreshResponse(jwtService.generateAccessToken(username, roles, user.getTokenVersion()));
    }

    @Override
    public void logout(String accessToken, LogoutRequest logoutRequest) {
        var accessJwt = jwtService.validate(accessToken);
        var refreshJwt = jwtService.validate(logoutRequest.refreshToken());

        blacklistService.blacklist(accessJwt.getId(), accessJwt.getExpiresAt());
        blacklistService.blacklist(refreshJwt.getId(), refreshJwt.getExpiresAt());
    }
}
