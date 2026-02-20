package dev.spiffocode.sigesapi.auth.application.service;

import dev.spiffocode.sigesapi.auth.presentation.*;

public interface BearerAuthService {

    AuthenticatedResponse login(LoginRequest request, String requestIp);

    RefreshResponse refresh(RefreshRequest refreshToken);

    void logout(String accessToken, LogoutRequest logoutRequest);

}
