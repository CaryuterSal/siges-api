package dev.spiffocode.sigesapi.notifications.application.service;

import java.util.Map;

public interface PushNotificationPort {
    /**
     * Sends a push notification to a specific user via a push notification service
     * (e.g. FCM).
     *
     * @param userId the ID of the user to send the notification to.
     * @param title  the title of the notification.
     * @param body   the message body.
     */
    void sendPushNotification(long userId, String title, String body, Map<String, String> metadata);

    /**
     * Sends a push notification to a topic via a push notification service
     * (e.g. FCM).
     *
     * @param topic  topic to send the notification to.
     * @param title  the title of the notification.
     * @param body   the message body.
     */
    void sendPushNotificationToTopic(String topic, String title, String body, Map<String, String> metadata);
}
