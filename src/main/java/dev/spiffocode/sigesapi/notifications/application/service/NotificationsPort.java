package dev.spiffocode.sigesapi.notifications.application.service;

public interface NotificationsPort {
    void sendNotification(long userId, SendNotificationCommand command);
    void sendNotificationToAdmins(SendNotificationCommand command);
}
