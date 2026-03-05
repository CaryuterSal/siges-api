package dev.spiffocode.sigesapi.notifications.application.service;

public interface NotificationsPort {
    void sendNotification(SendNotificationCommand command);
}
