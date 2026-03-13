package dev.spiffocode.sigesapi.notifications.domain.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum Type {

        COMMENT_ON_RESERVATION(
                        "Nuevo comentario en reservación",
                        "Un usuario dejó un comentario sobre la reservación",
                true,
                NotificationCategory.RESERVATIONS, false),
        RESERVATION_RESCHEDULE(
                        "Cambio de horario en reservación",
                        "La reservación vio un cambio en el horario y requiere tu aprobación",
                true,
                NotificationCategory.RESERVATIONS,

                false),
        RESERVATION_REMINDER(
                        "Recordatorio de reservación",
                        "No olvides tu próxima cita",
                false,
                NotificationCategory.RESERVATIONS,  // Could be mandatory, but we allow user to disable
                false),
        RESERVATION_CREATED(
                        "Nueva solicitud de reservación",
                        "Un usuario ha solicitado una reservación que requiere tu aprobación.",
                true,
                NotificationCategory.RESERVATIONS, false),
        RESERVATION_APPROVED(
                        "Reservación aprobada",
                        "Tu solicitud de reservación fue aprobada.",
                false,
                NotificationCategory.RESERVATIONS, false),
        RESERVATION_REJECTED(
                        "Reservación rechazada",
                        "Tu solicitud de reservación fue rechazada.",
                false,
                NotificationCategory.RESERVATIONS, false),
        RESERVATION_CANCELLED(
                        "Reservación cancelada",
                        "Una reservación ha sido cancelada.",
                true,
                NotificationCategory.RESERVATIONS, false),
        PASSWORD_CHANGED(
                        "Contraseña actualizada",
                        "La contraseña de tu cuenta ha sido actualizada recientemente.",
                false,
                NotificationCategory.ACCOUNT_SECURITY,
                true),
        LOGIN_NEW_DEVICE(
                        "Inicio de sesión en nuevo dispositivo",
                        "Se detectó un nuevo inicio de sesión en tu cuenta.",
                false,
                NotificationCategory.ACCOUNT_SECURITY,
                true);

        private final String defaultTitle;
        private final String defaultMessage;
        private final boolean adminTopic;
        private final NotificationCategory category;
        private final boolean mandatory;

        Type(String title, String message, boolean adminTopic, NotificationCategory category, boolean mandatory) {
                this.defaultTitle = title;
                this.defaultMessage = message;
                this.adminTopic = adminTopic;
                this.category = category;
                this.mandatory = mandatory;
        }

        public static List<Type> adminTopics(){
            return Arrays.stream(values()).filter(Type::isAdminTopic).toList();
        }

}
