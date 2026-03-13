package dev.spiffocode.sigesapi.notifications.infrastructure.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import dev.spiffocode.sigesapi.notifications.application.mapper.PushTokenMapper;
import dev.spiffocode.sigesapi.notifications.application.service.PushTokenService;
import dev.spiffocode.sigesapi.notifications.domain.model.PushToken;
import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import dev.spiffocode.sigesapi.notifications.domain.repository.PushTokenRepository;
import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.User;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import dev.spiffocode.sigesapi.users.presentation.dto.PushTokenRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushTokenServiceImpl implements PushTokenService {

    private final PushTokenRepository pushTokenRepository;
    private final UserRepository userRepository;
    private final PushTokenMapper mapper;
    private final FirebaseMessaging fcm;

    @Override
    @Transactional
    public void registerToken(Long userId, PushTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        PushToken token = pushTokenRepository.findById(request.token()).orElse(null);

        token = token == null ? mapper.toEntity(request, user) :  mapper.updateEntity(request, user, token);

        token = pushTokenRepository.save(token);

        if (user instanceof Admin) {
            try {
                PushToken finalToken = token;
                Type.adminTopics()
                    .forEach(type -> {
                        fcm.subscribeToTopicAsync(List.of(finalToken.getToken()), type.name());
                        log.info("Token {} subscribed to admins topic", finalToken.getToken());
                    });
            } catch (Exception e) {
                log.warn("Failed to subscribe token to topic admins: {}", e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void unregisterToken(Long userId, String tokenStr) {
        pushTokenRepository.findById(tokenStr).ifPresent(pushToken -> {
            if (pushToken.getUser().getId().equals(userId)) {
                pushTokenRepository.delete(pushToken);
                try {
                    Type.adminTopics()
                            .forEach(type -> {
                                fcm.unsubscribeFromTopicAsync(List.of(pushToken.getToken()), type.name());
                                log.info("Token {} subscribed to admins topic", pushToken.getToken());
                            });
                    fcm.unsubscribeFromTopicAsync(List.of(tokenStr), "admins");
                    log.info("Token {} unsubscribed from admins topic", tokenStr);
                } catch (Exception e) {
                    log.warn("Failed to unsubscribe token from topic admins: {}", e.getMessage());
                }
            }
        });
    }
}
