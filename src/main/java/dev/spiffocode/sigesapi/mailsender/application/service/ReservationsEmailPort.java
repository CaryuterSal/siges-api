package dev.spiffocode.sigesapi.mailsender.application.service;

import dev.spiffocode.sigesapi.reservations.domain.model.Status;

public interface ReservationsEmailPort {
    void sendReservationCreatedEmail(String email, long reservationId);

    void sendNewReservationRequestEmail(String email, String petitionerName, long reservationId);

    void sendReservationResolutionEmail(String email, Status status, long reservationId);

    void sendReservationCancelledEmail(String email, long reservationId);

    void sendReservationRescheduledEmail(String email, long reservationId);
}
