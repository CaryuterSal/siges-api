package dev.spiffocode.sigesapi.notifications.infrastructure.service.impl;

import dev.spiffocode.sigesapi.mailsender.application.service.ReservationsEmailPort;
import dev.spiffocode.sigesapi.notifications.application.service.NotificationsPort;
import dev.spiffocode.sigesapi.notifications.application.service.PushNotificationPort;
import dev.spiffocode.sigesapi.notifications.application.service.SendNotificationCommand;
import dev.spiffocode.sigesapi.notifications.domain.model.Notification;
import dev.spiffocode.sigesapi.notifications.domain.model.ReadStatus;
import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import dev.spiffocode.sigesapi.notifications.domain.repository.NotificationRepository;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.users.application.service.UserManagementService;
import dev.spiffocode.sigesapi.users.domain.model.User;
import dev.spiffocode.sigesapi.users.domain.repository.AdminRepository;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import dev.spiffocode.sigesapi.users.presentation.dto.NotificationPreferenceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsPortImpl implements NotificationsPort {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final NotificationRepository notificationRepository;
    private final UserManagementService userManagementService;
    private final ReservationsEmailPort reservationsEmailPort;
    private final PushNotificationPort pushNotificationPort;

    @Async
    @Transactional
    @Override
    public void sendNotification(long userId, SendNotificationCommand command) {
        sendNotificationInternal(userId, command, false);
    }

    private void sendNotificationInternal(long userId, SendNotificationCommand command, boolean skipPush) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Cannot send notification. User with id {} not found", userId);
            return;
        }

        Type type = command.type();
        boolean sendEmail = type.isMandatory();
        boolean sendInApp = type.isMandatory();

        if (!type.isMandatory()) {
            List<NotificationPreferenceResponse> preferences = userManagementService.getNotificationPreferences(userId);
            NotificationPreferenceResponse pref = preferences.stream()
                    .filter(p -> p.type() == type)
                    .findFirst()
                    .orElse(null);

            if (pref != null) {
                sendEmail = pref.emailEnabled();
                sendInApp = pref.inAppEnabled();
            } else {
                sendEmail = true;
                sendInApp = true;
            }
        }

        if (sendInApp) {
            String notificationTitle = command.title() != null ? command.title() : type.getDefaultTitle();
            String notificationMessage = command.message() != null ? command.message() : type.getDefaultMessage();

            Notification notification = Notification.builder()
                    .user(user)
                    .type(type)
                    .title(notificationTitle)
                    .body(notificationMessage)
                    .readStatus(ReadStatus.UNREAD)
                    .build();
            notification = notificationRepository.save(notification);

            Map<String, String> metadata = command.metadata() == null ? new HashMap<>() : new HashMap<>(command.metadata());
            metadata.put("id", notification.getId().toString());

            notification.setMetadata(metadata);
            notification = notificationRepository.save(notification);

            if (!skipPush) {
                pushNotificationPort.sendPushNotification(
                        userId,
                        notificationTitle,
                        notificationMessage,
                        metadata);
            }
        }

        if (sendEmail) {
            String email = user.getEmail();
            Long reservationId = command.entityId();

            if (reservationId != null) {
                switch (type) {
                    case RESERVATION_CREATED:
                        reservationsEmailPort.sendReservationCreatedEmail(email, reservationId);
                        break;
                    case RESERVATION_APPROVED:
                        reservationsEmailPort.sendReservationResolutionEmail(email,
                                Status.APPROVED, reservationId);
                        break;
                    case RESERVATION_REJECTED:
                        reservationsEmailPort.sendReservationResolutionEmail(email,
                                Status.REJECTED, reservationId);
                        break;
                    case RESERVATION_CANCELLED:
                        reservationsEmailPort.sendReservationCancelledEmail(email, reservationId);
                        break;
                    case RESERVATION_RESCHEDULE:
                        reservationsEmailPort.sendReservationRescheduledEmail(email, reservationId);
                        break;
                    default:
                        log.info("Email routing for type {} is not explicitly mapped for reservation {}", type,
                                reservationId);
                }
            } else {
                log.warn("Could not extract reservation ID (entityId) for command type {}, skipping email.", type);
            }
        }
    }

    @Async
    @Transactional
    @Override
    public void sendNotificationToAdmins(SendNotificationCommand command) {
        var admins = adminRepository.findAll();
        for (User admin : admins) {
            sendNotificationInternal(admin.getId(), command, true);
        }

        String topicName = Type.adminTopics().stream()
                .filter(type -> type.name().equals(command.type().name()))
                .findFirst().orElse(command.type()).name();

        String title = command.title() != null ? command.title() : command.type().getDefaultTitle();
        String message = command.message() != null ? command.message() : command.type().getDefaultMessage();

        pushNotificationPort.sendPushNotificationToTopic(
                topicName,
                title,
                message,
                command.metadata() == null ? java.util.Collections.emptyMap() : command.metadata());
    }
}
