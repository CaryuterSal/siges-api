package dev.spiffocode.sigesapi.reservations.infrastructure.service.impl;

import dev.spiffocode.sigesapi.notifications.application.service.SendNotificationCommand;
import dev.spiffocode.sigesapi.reservations.domain.model.*;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationExceptionRepository;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;

// application/usecase/ModifyOccurrenceUseCase.java
@UseCase
@RequiredArgsConstructor
public class ModifyOccurrenceUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationExceptionRepository exceptionRepository;
    private final SendNotificationUseCase sendNotificationUseCase;
    private final EmailService emailService;
    private final RecurrenceDateGenerator recurrenceDateGenerator;
    private final Clock clock;

    @Value("${reservation.cancellation.min-advance-hours:24}")
    private int minAdvanceHours;

    @Transactional
    public SingleReservation execute(ModifyOccurrenceCommand cmd, Long requesterId) {

        // 1. Cargar la serie
        Reservation reservation = reservationRepository.findById(cmd.recurringReservationId())
            .orElseThrow(() -> new ReservationNotFoundException(cmd.recurringReservationId()));

        if (!(reservation instanceof RecurringReservation recurring))
            throw new InvalidReservationTypeException(
                "Solo se pueden modificar ocurrencias de reservaciones recurrentes");

        // 2. Validaciones
        validateSeriesIsActive(recurring);
        validateOccurrenceDate(recurring, cmd.originalDate());
        validateNotAlreadyModified(recurring, cmd.originalDate());
        validateAdvanceTime(cmd.originalDate(), recurring.getStartTime());
        validateNewSlotOverlap(recurring, cmd);

        // 3. Crear la SingleReservation de reemplazo
        SingleReservation replacement = SingleReservation.builder()
            .applicant(recurring.getApplicant())
            .reservable(recurring.getReservable())
            .date(cmd.newDate())
            .startTime(cmd.newStartTime())
            .endTime(cmd.newEndTime())
            .status(Status.PENDING) // requiere aprobación del admin
            .build();

        SingleReservation savedReplacement =
            (SingleReservation) reservationRepository.save(replacement);

        // 4. Crear la excepción que excluye la fecha original de la serie
        ReservationException exception = ReservationException.builder()
            .reservation(recurring)
            .excludedDate(cmd.originalDate())
            .type(ExceptionType.RESCHEDULED)
            .replacement(savedReplacement)
            .build();

        exceptionRepository.save(exception);

        // 5. Notificar admins — necesitan aprobar la modificación
        notifyAdmins(recurring, cmd.originalDate(), savedReplacement);

        return savedReplacement;
    }

    // -------------------------
    // Validaciones
    // -------------------------

    private void validateSeriesIsActive(RecurringReservation reservation) {
        if (reservation.getStatus() != Status.APPROVED
                && reservation.getStatus() != Status.PENDING)
            throw new InvalidReservationStatusException(
                "No se puede modificar una ocurrencia de una serie en estado "
                + reservation.getStatus()
            );
    }

    private void validateOccurrenceDate(
            RecurringReservation reservation, LocalDate date) {

        boolean inRange = !date.isBefore(reservation.getSeriesDateFrom())
            && !date.isAfter(reservation.getSeriesDateTo());

        if (!inRange)
            throw new OccurrenceDateOutOfRangeException(date, reservation);

        // Verificar que la fecha es una ocurrencia real de la regla
        List<LocalDate> validDates = recurrenceDateGenerator.generate(
            reservation.getFrequency(),
            reservation.getRecurrences().stream()
                .map(ReservationRecurrence::getDayOfWeek)
                .toList(),
            reservation.getSeriesDateFrom(),
            reservation.getSeriesDateTo()
        );

        if (!validDates.contains(date))
            throw new InvalidOccurrenceDateException(date, reservation);
    }

    private void validateNotAlreadyModified(
            RecurringReservation reservation, LocalDate date) {

        exceptionRepository.findByReservationAndExcludedDate(reservation, date)
            .ifPresent(existing -> {
                throw switch (existing.getType()) {
                    case CANCELLED -> new OccurrenceAlreadyCancelledException(date);
                    case RESCHEDULED -> new OccurrenceAlreadyModifiedException(date);
                };
            });
    }

    private void validateAdvanceTime(LocalDate date, LocalTime startTime) {
        LocalDateTime occurrenceStart = date.atTime(startTime);
        long hoursUntil = ChronoUnit.HOURS.between(
            LocalDateTime.now(clock), occurrenceStart);

        if (hoursUntil < minAdvanceHours)
            throw new CancellationTooLateException(minAdvanceHours, hoursUntil);
    }

    private void validateNewSlotOverlap(
            RecurringReservation series, ModifyOccurrenceCommand cmd) {

        // Verificar overlap del nuevo horario — ignorando la propia serie
        // en la fecha original (porque esa ocurrencia se está excluyendo)
        boolean singleOverlap = reservationRepository.existsSingleOverlapExcluding(
            series.getReservable().getId(),
            cmd.newDate(),
            cmd.newStartTime(),
            cmd.newEndTime(),
            List.of(Status.PENDING, Status.APPROVED),
            null // no hay ID a excluir aún porque el replacement aún no existe
        );

        if (singleOverlap)
            throw new ReservationOverlapException(List.of(cmd.newDate()));

        // Verificar contra otras series — excluyendo la serie actual
        // si newDate coincide con una ocurrencia de la misma serie
        List<RecurringReservation> conflictingSeries = reservationRepository
            .findActiveRecurringByReservableAndDate(
                series.getReservable().getId(),
                cmd.newDate(),
                cmd.newDate().getDayOfWeek()
            )
            .stream()
            .filter(s -> !s.getId().equals(series.getId())) // excluir la propia serie
            .filter(s -> s.getStartTime().isBefore(cmd.newEndTime())
                      && s.getEndTime().isAfter(cmd.newStartTime()))
            .filter(s -> s.getExceptions().stream()
                .noneMatch(e -> e.getExcludedDate().equals(cmd.newDate())))
            .toList();

        if (!conflictingSeries.isEmpty())
            throw new ReservationOverlapException(List.of(cmd.newDate()));
    }

    // -------------------------
    // Notificaciones
    // -------------------------

    @Async
    protected void notifyAdmins(
            RecurringReservation series,
            LocalDate originalDate,
            SingleReservation replacement) {

        sendNotificationUseCase.notifyAllAdmins(
            new SendNotificationCommand(
                NotificationType.RESERVATION_CREATED, // reutilizas este tipo
                replacement.getId()
            )
        );

        emailService.sendOccurrenceModificationRequested(
            series.getApplicant(),
            series,
            originalDate,
            replacement
        );
    }
}