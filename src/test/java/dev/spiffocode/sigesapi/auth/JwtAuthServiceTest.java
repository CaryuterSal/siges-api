package dev.spiffocode.sigesapi.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.spiffocode.sigesapi.UnitTestClass;
import dev.spiffocode.sigesapi.auth.presentation.*;
import dev.spiffocode.sigesapi.auth.infrastructure.BlacklistedJwtService;
import dev.spiffocode.sigesapi.auth.infrastructure.JwtService;
import dev.spiffocode.sigesapi.auth.infrastructure.TokenBlacklistService;
import dev.spiffocode.sigesapi.common.infrastructure.exceptions.JwtBlacklistedException;
import dev.spiffocode.sigesapi.users.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@UnitTestClass
class JwtAuthServiceTest {

    @Mock
    JwtService jwtService;
    @Mock
    UserDetailsService userDetailsService;
    @Mock
    AuthenticationManager authManager;
    @Mock
    TokenBlacklistService blacklistService;

    @InjectMocks
    BlacklistedJwtService service;


    @Test
    void login_ok_returnsTokens() {
        LoginRequest req = new LoginRequest("mail@test.com", "123");

        Authentication authentication = mock(Authentication.class);
        UserDetails user = mock(UserDetails.class);
        when(user.getUsername()).thenReturn("mail@test.com");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER"))).when(user).getAuthorities();
        when(authentication.getPrincipal()).thenReturn(user);
        when(authManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateAccessToken(anyString(), any())).thenReturn("access");
        when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh");
        AuthenticatedResponse res = service.login(req);

        assertEquals("access", res.accessToken());
        assertEquals("refresh", res.refreshToken());
    }

    @Test
    void login_userNotFound_throws() {
        when(authManager.authenticate(any())).thenThrow(BadCredentialsException.class);
        assertThrows(BadCredentialsException.class,
                () -> service.login(new LoginRequest("x", "x")));
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

