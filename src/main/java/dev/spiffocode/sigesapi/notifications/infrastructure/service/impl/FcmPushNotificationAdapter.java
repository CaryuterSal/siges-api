package dev.spiffocode.sigesapi.notifications.infrastructure.service.impl;

import com.google.firebase.messaging.*;
import dev.spiffocode.sigesapi.notifications.application.service.PushNotificationPort;
import dev.spiffocode.sigesapi.notifications.domain.model.PushToken;
import dev.spiffocode.sigesapi.notifications.domain.repository.PushTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter for sending Firebase Cloud Messaging (FCM) Push Notifications.
 * Note: Actual Firebase Admin SDK dependency and logic should be injected here.
 * For now, it logs the intent to send.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FcmPushNotificationAdapter implements PushNotificationPort {

    private final PushTokenRepository tokenRepository;
    private final FirebaseMessaging fcm;

    @Async
    @Override
    public void sendPushNotification(long userId, String title, String body, Map<String, String> metadata) {

        List<PushToken> pushTokens = tokenRepository.findByUserId(userId);
        List<String> tokens = pushTokens.stream().map(PushToken::getToken).toList();

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(metadata)
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = fcm.sendEachForMulticast(message);
            log.info("Notification sent to user {}: {}/{} successful",
                    userId, response.getSuccessCount(), tokens.size());

            if (response.getFailureCount() > 0) {
                List<String> invalidTokens = extractInvalidTokens(response, tokens);

                if (!invalidTokens.isEmpty()) {
                    tokenRepository.deleteAllByTokenIn(invalidTokens);
                    log.info("Deleted {} invalid tokens for user {}", invalidTokens.size(), userId);
                }
            }
        } catch (FirebaseMessagingException ex) {
            log.warn("Failed User notification '{}' send", title);
            log.warn(ex.getMessage());
        }
    }

    private List<String> extractInvalidTokens(BatchResponse response, List<String> tokens) {
        List<String> invalidTokens = new ArrayList<>();

        List<SendResponse> responses = response.getResponses();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse sendResponse = responses.get(i);
            if (!sendResponse.isSuccessful()) {
                MessagingErrorCode errorCode = sendResponse.getException().getMessagingErrorCode();
                if (errorCode == MessagingErrorCode.UNREGISTERED
                        || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                    invalidTokens.add(tokens.get(i));
                }
            }
        }
        return invalidTokens;
    }

    @Async
    @Override
    public void sendPushNotificationToTopic(String topic, String title, String body, Map<String, String> metadata) {

        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(metadata)
                .setTopic(topic)
                .build();

        try {
            fcm.send(message);
            log.info("Notification '{}' sent to topic {}", title, topic);
        } catch (FirebaseMessagingException ex) {
            log.warn("Failed Topic notification '{}' send", title);
            log.warn(ex.getMessage());
        }
    }
}
