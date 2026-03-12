package dev.spiffocode.sigesapi.notifications.domain.model;

import lombok.Getter;

@Getter
public enum Type {

        COMMENT_ON_RESERVATION(
                        "Nuevo comentario en reservación",
                        "Un usuario dejó un comentario sobre la reservación",
                        NotificationCategory.RESERVATIONS,
                        false),
        RESERVATION_RESCHEDULE(
                        "Cambio de horario en reservación",
                        "La reservación vio un cambio en el horario y requiere tu aprobación",
                        NotificationCategory.RESERVATIONS,
                        false

        ),
        RESERVATION_REMINDER(
                        "Recordatorio de reservación",
                        "No olvides tu próxima cita",
                        NotificationCategory.RESERVATIONS,
                        false // Could be mandatory, but we allow user to disable
        ),
        RESERVATION_CREATED(
                        "Nueva solicitud de reservación",
                        "Un usuario ha solicitado una reservación que requiere tu aprobación.",
                        NotificationCategory.RESERVATIONS,
                        false),
        RESERVATION_APPROVED(
                        "Reservación aprobada",
                        "Tu solicitud de reservación fue aprobada.",
                        NotificationCategory.RESERVATIONS,
                        false),
        RESERVATION_REJECTED(
                        "Reservación rechazada",
                        "Tu solicitud de reservación fue rechazada.",
                        NotificationCategory.RESERVATIONS,
                        false),
        RESERVATION_CANCELLED(
                        "Reservación cancelada",
                        "Una reservación ha sido cancelada.",
                        NotificationCategory.RESERVATIONS,
                        false),
        PASSWORD_CHANGED(
                        "Contraseña actualizada",
                        "La contraseña de tu cuenta ha sido actualizada recientemente.",
                        NotificationCategory.ACCOUNT_SECURITY,
                        true // Mandatory, user cannot turn off security alerts
        ),
        LOGIN_NEW_DEVICE(
                        "Inicio de sesión en nuevo dispositivo",
                        "Se detectó un nuevo inicio de sesión en tu cuenta.",
                        NotificationCategory.ACCOUNT_SECURITY,
                        true // Mandatory
        );

        private final String defaultTitle;
        private final String defaultMessage;
        private final NotificationCategory category;
        private final boolean mandatory;

        Type(String title, String message, NotificationCategory category, boolean mandatory) {
                this.defaultTitle = title;
                this.defaultMessage = message;
                this.category = category;
                this.mandatory = mandatory;
        }

}
