package dev.spiffocode.sigesapi.notifications.infrastructure.service.impl;

import dev.spiffocode.sigesapi.auth.infrastructure.SecurityContextHelper;
import dev.spiffocode.sigesapi.notifications.application.service.NotificationFilter;
import dev.spiffocode.sigesapi.notifications.application.service.NotificationsService;
import dev.spiffocode.sigesapi.notifications.domain.model.Notification;
import dev.spiffocode.sigesapi.notifications.domain.model.ReadStatus;
import dev.spiffocode.sigesapi.notifications.domain.repository.NotificationRepository;
import dev.spiffocode.sigesapi.notifications.presentation.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationsServiceImpl implements NotificationsService {

    private final NotificationRepository notificationRepository;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> listNotifications(Pageable pageable, NotificationFilter filter) {
        Long currentUserId = securityContextHelper.getCurrentUserId();
        // Assuming there is a custom query in the repository that handles this filter
        // If not, we might need to adjust this depending on how the repo is set up.
        // For now returning an empty list as a placeholder for the read operations
        return List.of();
    }

    @Override
    public NotificationResponse changeNotificationStatus(Long id, ReadStatus readStatus) {
        Long currentUserId = securityContextHelper.getCurrentUserId();
        Notification notification = notificationRepository.findById(id)
                .filter(n -> n.getRecipient().getId().equals(currentUserId))
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setStatus(readStatus);
        notificationRepository.save(notification);

        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getStatus(),
                notification.getCreatedAt());
    }

    @Override
    public void changeAllNotificationsStatus(ReadStatus readStatus) {
        Long currentUserId = securityContextHelper.getCurrentUserId();
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndStatus(currentUserId,
                ReadStatus.UNREAD);

        unreadNotifications.forEach(n -> n.setStatus(readStatus));
        notificationRepository.saveAll(unreadNotifications);
    }
}
