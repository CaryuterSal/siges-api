package dev.spiffocode.sigesapi.notifications.application.service;

import dev.spiffocode.sigesapi.users.presentation.dto.PushTokenRequest;

public interface PushTokenService {
    void registerToken(Long userId, PushTokenRequest request);

    void unregisterToken(Long userId, String token);
}
