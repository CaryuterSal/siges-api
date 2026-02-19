package dev.spiffocode.sigesapi.auth.application.service;

import dev.spiffocode.sigesapi.auth.presentation.*;

public interface JwtAuthService {

    AuthenticatedResponse login(LoginRequest request);

    RefreshResponse refresh(RefreshRequest refreshToken);

    void logout(String accessToken, LogoutRequest logoutRequest);

}
