package dev.spiffocode.sigesapi.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.spiffocode.sigesapi.UnitTestClass;
import dev.spiffocode.sigesapi.auth.domain.exception.InvalidCredentialsException;
import dev.spiffocode.sigesapi.auth.domain.exception.JwtBlacklistedException;
import dev.spiffocode.sigesapi.auth.domain.model.LogInAttemptsRepository;
import dev.spiffocode.sigesapi.auth.infrastructure.service.impl.BlacklistedJwtAuthService;
import dev.spiffocode.sigesapi.auth.infrastructure.JwtService;
import dev.spiffocode.sigesapi.auth.infrastructure.LogInAttemptsProperties;
import dev.spiffocode.sigesapi.auth.infrastructure.TokenBlacklistService;
import dev.spiffocode.sigesapi.auth.infrastructure.service.impl.LoginAttemptRecorder;
import dev.spiffocode.sigesapi.auth.presentation.dto.*;
import dev.spiffocode.sigesapi.users.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTestClass
class BearerAuthServiceTest {

    @Mock
    LoginAttemptRecorder loginAttemptRecorder;
    @Mock
    JwtService jwtService;
    @Mock
    UserDetailsService userDetailsService;
    @Mock
    AuthenticationManager authManager;
    @Mock
    TokenBlacklistService blacklistService;
    @Mock
    Clock clock;
    @Mock
    LogInAttemptsProperties logInAttemptsProperties;
    @Mock
    LogInAttemptsRepository logInAttemptsRepository;

    @InjectMocks
    BlacklistedJwtAuthService service;

    @Test
    void login_ok_returnsTokens() {
        LoginRequest req = new LoginRequest("mail@test.com", "123");

        Authentication authentication = mock(Authentication.class);
        UserDetails user = mock(UserDetails.class);


        when(logInAttemptsRepository.countRecentFailuresSinceLastSuccess(any(), any(), any())).thenReturn(0L);
        when(logInAttemptsProperties.getLockMinutes()).thenReturn(10);
        when(logInAttemptsProperties.getMaxAttempts()).thenReturn(10);
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        when(user.getUsername()).thenReturn("mail@test.com");
        when(authentication.getPrincipal()).thenReturn(user);
        when(authManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateAccessToken(anyString(), any())).thenReturn("access");
        when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh");
        AuthenticatedResponse res = service.login(req, "197.168.1.1");

        assertEquals("access", res.accessToken());
        assertEquals("refresh", res.refreshToken());
    }

    @Test
    void login_userNotFound_throws() {

        when(logInAttemptsRepository.countRecentFailuresSinceLastSuccess(any(), any(), any())).thenReturn(0L);
        when(logInAttemptsProperties.getLockMinutes()).thenReturn(10);
        when(logInAttemptsProperties.getMaxAttempts()).thenReturn(10);
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));

        when(authManager.authenticate(any())).thenThrow(BadCredentialsException.class);
        assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("x", "x"), "197.168.1.1"));
    }


    @Test
    void refresh_validToken_returnsNewAccess() {

        when(jwtService.isRefreshToken(any())).thenReturn(true);
        when(jwtService.extractUsername("r")).thenReturn("mock@user.com");

        when(userDetailsService.loadUserByUsername(any())).thenReturn(mock(User.class));
        when(jwtService.generateAccessToken(anyString(), any())).thenReturn("newAccess");

        RefreshResponse res =
                service.refresh(new RefreshRequest("r"));

        assertEquals("newAccess", res.accessToken());
    }

    @Test
    void refresh_not_refresh_token_throws() {
        when(jwtService.isRefreshToken(any())).thenReturn(false);

        assertThrows(JWTVerificationException.class,
                () -> service.refresh(new RefreshRequest("r"))
        );
    }

    @Test
    void refresh_blacklisted_fails(){

        when(jwtService.isRefreshToken(any())).thenReturn(true);
        when(blacklistService.isBlacklisted(any())).thenReturn(true);

        assertThrowsExactly(JwtBlacklistedException.class,
                () -> service.refresh(new RefreshRequest("r"))
        );
    }

    @Test
    void refresh_invalidToken_throws() {
        when(jwtService.isRefreshToken(any())).thenReturn(true);
        when(jwtService.extractUsername("bad")).thenThrow(JWTVerificationException.class);

        assertThrows(JWTVerificationException.class,
                () -> service.refresh(new RefreshRequest("bad")));
    }

    @Test
    void logout_blacklistsToken() {
        DecodedJWT mockJWT = mock(DecodedJWT.class);
        when(mockJWT.getExpiresAt()).thenReturn(Date.from(Instant.now().plus(Duration.ofDays(1))));
        when(mockJWT.getId()).thenReturn(UUID.randomUUID().toString());

        when(jwtService.validate(any())).thenReturn(mockJWT);
        service.logout("access", new LogoutRequest("refresh"));

        verify(blacklistService, times(2)).blacklist(any(), any());
    }
}

