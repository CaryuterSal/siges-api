package dev.spiffocode.sigesapi.notifications.infrastructure.service.impl;

import dev.spiffocode.sigesapi.auth.infrastructure.SecurityContextHelper;
import dev.spiffocode.sigesapi.notifications.application.mapper.NotificationMapper;
import dev.spiffocode.sigesapi.notifications.application.service.NotificationFilter;
import dev.spiffocode.sigesapi.notifications.application.service.NotificationsService;
import dev.spiffocode.sigesapi.notifications.domain.model.Notification;
import dev.spiffocode.sigesapi.notifications.domain.model.ReadStatus;
import dev.spiffocode.sigesapi.notifications.domain.repository.NotificationRepository;
import dev.spiffocode.sigesapi.notifications.domain.specification.NotificationSpecifications;
import dev.spiffocode.sigesapi.notifications.presentation.NotificationResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationsServiceImpl implements NotificationsService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull NotificationResponse> listNotifications(Pageable pageable, NotificationFilter filter) {
        Long currentUserId = securityContextHelper.getCurrentUserId();
        return notificationRepository.findAll(NotificationSpecifications.byFilter(filter, currentUserId), pageable)
                .map(notificationMapper::toDto);
    }

    @Override
    public NotificationResponse changeNotificationStatus(Long id, ReadStatus readStatus) {
        Long currentUserId = securityContextHelper.getCurrentUserId();
        Notification notification = notificationRepository.findById(id)
                .filter(n -> n.getUser().getId().equals(currentUserId))
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setReadStatus(readStatus);
        notification = notificationRepository.save(notification);

        return notificationMapper.toDto(notification);
    }

    @Override
    public void changeAllNotificationsStatus(ReadStatus readStatus) {
        Long currentUserId = securityContextHelper.getCurrentUserId();
        List<Notification> unreadNotifications = notificationRepository.findByUserId(currentUserId);

        unreadNotifications.forEach(n -> n.setReadStatus(readStatus));
        notificationRepository.saveAll(unreadNotifications);
    }
}
