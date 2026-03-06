package dev.spiffocode.sigesapi.reservations.infrastructure.service.impl;

import dev.spiffocode.sigesapi.notifications.application.service.SendNotificationCommand;
import dev.spiffocode.sigesapi.reservations.domain.model.*;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationExceptionRepository;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;

import java.util.stream.Collectors;

// application/usecase/CancelReservationUseCase.java
@UseCase
@RequiredArgsConstructor
public class CancelReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationExceptionRepository exceptionRepository;
    private final SendNotificationUseCase sendNotificationUseCase;
    private final EmailService emailService;
    private final Clock clock;

    @Value("${reservation.cancellation.min-advance-hours:24}")
    private int minAdvanceHours;

    @Transactional
    public void execute(CancelReservationCommand cmd, Long requesterId) {

        Reservation reservation = reservationRepository.findById(cmd.reservationId())
            .orElseThrow(() -> new ReservationNotFoundException(cmd.reservationId()));

        validateCancellable(reservation);
        validateRequesterPermission(reservation, requesterId);

        if (reservation instanceof SingleReservation single) {
            cancelSingle(single, cmd);
        } else if (reservation instanceof RecurringReservation recurring) {
            cancelRecurring(recurring, cmd);
        }

        notifyAdmins(reservation);
        emailService.sendReservationCancelled(reservation.getApplicant(), reservation);
    }

    // -------------------------
    // Cancelación de reservación única
    // -------------------------

    private void cancelSingle(SingleReservation reservation, CancelReservationCommand cmd) {
        validateAdvanceTime(reservation.getDate(), reservation.getStartTime());
        reservation.cancel(cmd.reason(), clock);
        reservationRepository.save(reservation);
    }

    // -------------------------
    // Cancelación de serie recurrente
    // -------------------------

    private void cancelRecurring(RecurringReservation reservation, CancelReservationCommand cmd) {
        switch (cmd.scope()) {
            case SINGLE_OCCURRENCE -> cancelSingleOccurrence(reservation, cmd);
            case THIS_AND_FUTURE -> cancelThisAndFuture(reservation, cmd);
            case ALL -> cancelAll(reservation, cmd);
        }
    }

    private void cancelSingleOccurrence(
            RecurringReservation reservation, CancelReservationCommand cmd) {

        validateOccurrenceDate(reservation, cmd.occurrenceDate());
        validateAdvanceTime(cmd.occurrenceDate(), reservation.getStartTime());

        // Verificar que no exista ya una excepción para esta fecha
        exceptionRepository.findByReservationAndExcludedDate(reservation, cmd.occurrenceDate())
            .ifPresent(e -> { throw new OccurrenceAlreadyCancelledException(cmd.occurrenceDate()); });

        ReservationException exception = ReservationException.builder()
            .reservation(reservation)
            .excludedDate(cmd.occurrenceDate())
            .type(ExceptionType.CANCELLED)
            .build();

        exceptionRepository.save(exception);
    }

    private void cancelThisAndFuture(
            RecurringReservation reservation, CancelReservationCommand cmd) {

        validateOccurrenceDate(reservation, cmd.occurrenceDate());
        validateAdvanceTime(cmd.occurrenceDate(), reservation.getStartTime());

        // Acortar la serie hasta el día anterior a la ocurrencia objetivo
        LocalDate newEndDate = cmd.occurrenceDate().minusDays(1);

        if (newEndDate.isBefore(reservation.getSeriesDateFrom())) {
            // Si la ocurrencia es la primera, cancelar toda la serie
            cancelAll(reservation, cmd);
            return;
        }

        reservation.setSeriesDateTo(newEndDate);
        reservationRepository.save(reservation);
    }

    private void cancelAll(RecurringReservation reservation, CancelReservationCommand cmd) {
        // Validar anticipación sobre la próxima ocurrencia futura
        LocalDate nextOccurrence = findNextOccurrence(reservation);
        if (nextOccurrence != null) {
            validateAdvanceTime(nextOccurrence, reservation.getStartTime());
        }

        reservation.cancel(cmd.reason(), clock);
        reservationRepository.save(reservation);
    }

    // -------------------------
    // Validaciones
    // -------------------------

    private void validateCancellable(Reservation reservation) {
        if (reservation.getStatus() == Status.FINISHED
                || reservation.getStatus() == Status.REJECTED
                || reservation.getStatus() == Status.CANCELLED) {
            throw new InvalidReservationStatusException(
                "No se puede cancelar una reservación en estado " + reservation.getStatus()
            );
        }
    }

    private void validateRequesterPermission(Reservation reservation, Long requesterId) {
        boolean isOwner = reservation.getApplicant().getId().equals(requesterId);
        // Los admins pueden cancelar cualquier reservación — eso se valida
        // con @PreAuthorize en el controlador, no aquí
        if (!isOwner)
            throw new UnauthorizedCancellationException(requesterId, reservation.getId());
    }

    private void validateAdvanceTime(LocalDate date, LocalTime startTime) {
        LocalDateTime occurrenceStart = date.atTime(startTime);
        LocalDateTime now = LocalDateTime.now(clock);
        long hoursUntil = ChronoUnit.HOURS.between(now, occurrenceStart);

        if (hoursUntil < minAdvanceHours)
            throw new CancellationTooLateException(minAdvanceHours, hoursUntil);
    }

    private void validateOccurrenceDate(RecurringReservation reservation, LocalDate date) {
        // Para MONTHLY, QUARTERLY, etc — verificar que la fecha
        // coincide con el día del mes de seriesDateFrom
        if (reservation.getFrequency() != RecurrenceFrequency.WEEKLY
                && reservation.getFrequency() != RecurrenceFrequency.BIWEEKLY) {

            boolean isValidMonthlyOccurrence = recurrenceDateGenerator
                    .generate(
                            reservation.getFrequency(),
                            List.of(),
                            reservation.getSeriesDateFrom(),
                            reservation.getSeriesDateTo()
                    )
                    .contains(date);

            if (!isValidMonthlyOccurrence)
                throw new InvalidOccurrenceDateException(date, reservation);
            return;
        }

        // Para WEEKLY/BIWEEKLY — verificar DayOfWeek
        boolean isValidOccurrence = reservation.getRecurrences().stream()
                .anyMatch(r -> r.getDayOfWeek() == date.getDayOfWeek());

        if (!isValidOccurrence)
            throw new InvalidOccurrenceDateException(date, reservation);
    }

    private LocalDate findNextOccurrence(RecurringReservation reservation) {
        LocalDate today = LocalDate.now(clock);
        Set<DayOfWeek> days = reservation.getRecurrences().stream()
            .map(ReservationRecurrence::getDayOfWeek)
            .collect(Collectors.toSet());

        LocalDate cursor = today.isAfter(reservation.getSeriesDateFrom())
            ? today : reservation.getSeriesDateFrom();

        while (!cursor.isAfter(reservation.getSeriesDateTo())) {
            if (days.contains(cursor.getDayOfWeek())) return cursor;
            cursor = cursor.plusDays(1);
        }
        return null; // serie ya terminó
    }

    // -------------------------
    // Notificaciones
    // -------------------------

    @Async
    protected void notifyAdmins(Reservation reservation) {
        sendNotificationUseCase.notifyAllAdmins(
            new SendNotificationCommand(
                NotificationType.RESERVATION_CANCELLED,
                reservation.getId()
            )
        );
    }
}