package dev.spiffocode.sigesapi.notifications.domain.model;

import lombok.Getter;

@Getter
public enum Type {

    RESERVATION_REMINDER(
            "Recordatorio de reservación",
            "No olvides tu próxima cita"
    ),
    RESERVATION_CREATED(
            "Nueva solicitud de reservación",
            "Un usuario ha solicitado una reservación que requiere tu aprobación."
    ),
    RESERVATION_APPROVED(
            "Reservación aprobada",
            "Tu solicitud de reservación fue aprobada."
    ),
    RESERVATION_REJECTED(
            "Reservación rechazada",
            "Tu solicitud de reservación fue rechazada."
    ),
    RESERVATION_CANCELLED(
            "Reservación cancelada",
            "Una reservación ha sido cancelada."
    );

    private final String defaultTitle;
    private final String defaultMessage;

    Type(String title, String message) {
        this.defaultTitle = title;
        this.defaultMessage = message;
    }

}
