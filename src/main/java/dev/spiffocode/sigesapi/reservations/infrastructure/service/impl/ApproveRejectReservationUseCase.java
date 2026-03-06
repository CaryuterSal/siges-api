package dev.spiffocode.sigesapi.reservations.infrastructure.service.impl;

import lombok.RequiredArgsConstructor;

// application/usecase/ApproveRejectReservationUseCase.java
@UseCase
@RequiredArgsConstructor
public class ApproveRejectReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final SendNotificationUseCase sendNotificationUseCase;
    private final EmailService emailService;
    private final Clock clock;

    @Transactional
    public void execute(UpdateReservationStatusCommand cmd) {

        Reservation reservation = reservationRepository.findById(cmd.reservationId())
            .orElseThrow(() -> new ReservationNotFoundException(cmd.reservationId()));

        // Solo reservaciones PENDING pueden ser aprobadas/rechazadas
        if (reservation.getStatus() != Status.PENDING)
            throw new InvalidReservationStatusException(
                "Solo se puede aprobar o rechazar una reservación en estado PENDING. " +
                "Estado actual: " + reservation.getStatus()
            );

        switch (cmd.action()) {
            case APPROVE -> approve(reservation, cmd.comment());
            case REJECT -> reject(reservation, cmd.comment());
        }

        reservationRepository.save(reservation);
        notifyApplicant(reservation, cmd.action());
    }

    // -------------------------
    // Aprobar
    // -------------------------

    private void approve(Reservation reservation, String comment) {
        reservation.approve(clock);

        if (comment != null && !comment.isBlank()) {
            reservation.addNote(comment);
        }
    }

    // -------------------------
    // Rechazar
    // -------------------------

    private void reject(Reservation reservation, String comment) {
        reservation.reject(comment, clock);
    }

    // -------------------------
    // Notificaciones
    // -------------------------

    @Async
    protected void notifyApplicant(Reservation reservation, ReservationAction action) {
        NotificationType type = action == ReservationAction.APPROVE
            ? NotificationType.RESERVATION_APPROVED
            : NotificationType.RESERVATION_REJECTED;

        sendNotificationUseCase.execute(
            new SendNotificationCommand(
                reservation.getApplicant().getId(),
                type
            )
        );

        emailService.sendReservationStatusChanged(
            reservation.getApplicant(),
            reservation,
            action
        );
    }
}