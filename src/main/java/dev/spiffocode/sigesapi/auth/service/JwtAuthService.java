package dev.spiffocode.sigesapi.auth.service;

import dev.spiffocode.sigesapi.auth.controller.dto.*;
import org.springframework.stereotype.Service;

public interface JwtAuthService {

    AuthenticatedResponse login(LoginRequest request);

    RefreshResponse refresh(RefreshRequest refreshToken);

    void logout(String accessToken, LogoutRequest logoutRequest);

}
