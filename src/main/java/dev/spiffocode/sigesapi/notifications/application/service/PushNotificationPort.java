package dev.spiffocode.sigesapi.notifications.application.service;

public interface PushNotificationPort {
    /**
     * Sends a push notification to a specific user via a push notification service
     * (e.g. FCM).
     *
     * @param userId the ID of the user to send the notification to.
     * @param title  the title of the notification.
     * @param body   the message body.
     */
    void sendPushNotification(long userId, String title, String body);
}
