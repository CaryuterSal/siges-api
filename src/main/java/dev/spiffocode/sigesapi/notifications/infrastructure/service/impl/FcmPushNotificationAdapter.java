package dev.spiffocode.sigesapi.notifications.infrastructure.service.impl;

import dev.spiffocode.sigesapi.notifications.application.service.PushNotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Adapter for sending Firebase Cloud Messaging (FCM) Push Notifications.
 * Note: Actual Firebase Admin SDK dependency and logic should be injected here.
 * For now, it logs the intent to send.
 */
@Slf4j
@Component
public class FcmPushNotificationAdapter implements PushNotificationPort {

    @Async
    @Override
    public void sendPushNotification(long userId, String title, String body) {
        // TODO: Implement actual FCM token retrieval and dispatch logic
        // Example:
        // List<String> fcmTokens = userTokenRepository.findTokensByUserId(userId);
        // Message message = Message.builder()
        // .setNotification(Notification.builder().setTitle(title).setBody(body).build())
        // ...

        log.info("Mock FCM Dispatch: Sending Push to userId={} | Title: '{}' | Body '{}'", userId, title, body);
    }
}
