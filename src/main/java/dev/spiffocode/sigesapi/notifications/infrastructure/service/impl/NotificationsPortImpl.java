package dev.spiffocode.sigesapi.notifications.infrastructure.service.impl;

import dev.spiffocode.sigesapi.mailsender.application.service.ReservationsEmailPort;
import dev.spiffocode.sigesapi.notifications.application.service.NotificationsPort;
import dev.spiffocode.sigesapi.notifications.application.service.SendNotificationCommand;
import dev.spiffocode.sigesapi.notifications.domain.model.Notification;
import dev.spiffocode.sigesapi.notifications.domain.model.ReadStatus;
import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import dev.spiffocode.sigesapi.notifications.domain.repository.NotificationRepository;
import dev.spiffocode.sigesapi.users.application.service.UserQueryService;
import dev.spiffocode.sigesapi.users.domain.model.User;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import dev.spiffocode.sigesapi.users.presentation.dto.NotificationPreferenceResponse;
import dev.spiffocode.sigesapi.users.application.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsPortImpl implements NotificationsPort {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final UserManagementService userManagementService;
    private final ReservationsEmailPort reservationsEmailPort;

    @Override
    public void sendNotification(long userId, SendNotificationCommand command) {
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
            Notification notification = Notification.builder()
                    .recipient(user)
                    .type(type)
                    .title(command.title() != null ? command.title() : type.getDefaultTitle())
                    .message(command.message() != null ? command.message() : type.getDefaultMessage())
                    .status(ReadStatus.UNREAD)
                    .build();
            notificationRepository.save(notification);
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
                                dev.spiffocode.sigesapi.reservations.domain.model.Status.APPROVED, reservationId);
                        break;
                    case RESERVATION_REJECTED:
                        reservationsEmailPort.sendReservationResolutionEmail(email,
                                dev.spiffocode.sigesapi.reservations.domain.model.Status.REJECTED, reservationId);
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

    @Override
    public void sendNotificationToAdmins(SendNotificationCommand command) {
        List<User> admins = userRepository.findAdmins();
        for (User admin : admins) {
            sendNotification(admin.getId(), command);
        }
    }
}
