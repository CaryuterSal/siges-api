package dev.spiffocode.sigesapi.notifications.application.service;

import dev.spiffocode.sigesapi.notifications.domain.model.ReadStatus;
import dev.spiffocode.sigesapi.notifications.presentation.NotificationResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationsService {
    Page<@NonNull NotificationResponse> listNotifications(Pageable pageable, NotificationFilter filter);
    NotificationResponse changeNotificationStatus(Long id, ReadStatus readStatus);
    void changeAllNotificationsStatus(ReadStatus readStatus);
}
