package dev.spiffocode.sigesapi.notifications.application.service;

import dev.spiffocode.sigesapi.notifications.domain.model.ReadStatus;
import dev.spiffocode.sigesapi.notifications.presentation.NotificationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationsService {
    List<NotificationResponse> listNotifications(Pageable pageable, NotificationFilter filter);
    NotificationResponse changeNotificationStatus(Long id, ReadStatus readStatus);
    void changeAllNotificationsStatus(ReadStatus readStatus);
}
