package dev.spiffocode.sigesapi.reservations.infrastructure.service.impl;

import dev.spiffocode.sigesapi.notifications.application.service.SendNotificationCommand;
import dev.spiffocode.sigesapi.reservables.domain.exception.ReservableNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservables.domain.repository.ReservableRepository;
import dev.spiffocode.sigesapi.reservations.domain.model.*;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import dev.spiffocode.sigesapi.users.domain.model.Applicant;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

// application/usecase/CreateReservationUseCase.java
@Service
@RequiredArgsConstructor
public class CreateReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservableRepository reservableRepository;
    private final ApplicantRepository applicantRepository;
    private final SendNotificationUseCase sendNotificationUseCase;
    private final EmailService emailService;
    private final Clock clock;

    @Value("${reservation.recurrence.max-weeks:52}")
    private int maxWeeks;

    @Transactional
    public Reservation execute(CreateReservationCommand cmd, Long applicantId) {

        // 1. Cargar entidades
        Reservable reservable = reservableRepository.findById(cmd.reservableId())
            .orElseThrow(() -> new ReservableNotFoundException(cmd.reservableId()));

        Applicant applicant = applicantRepository.findById(applicantId)
            .orElseThrow(() -> new ApplicantNotFoundException(applicantId));

        // 2. Validaciones de negocio
        validateAdvanceTime(reservable, cmd, clock);
        validateStudentRestrictions(applicant, reservable, cmd);
        validateSeriesDuration(cmd);

        // 3. Crear la reservación según tipo
        Reservation reservation = switch (cmd.recurrenceType()) {
            case SINGLE -> createSingle(cmd, applicant, reservable);
            case RECURRING -> createRecurring(cmd, applicant, reservable);
        };

        Reservation saved = reservationRepository.save(reservation);

        // 4. Notificaciones (async)
        notifyAdmins(saved);
        emailService.sendReservationCreated(applicant, reservable, saved);

        return saved;
    }

    // -------------------------
    // Creación
    // -------------------------

    private SingleReservation createSingle(
            CreateReservationCommand cmd, Applicant applicant, Reservable reservable) {

        validateNoSingleOverlap(cmd.reservableId(), cmd.date(), cmd.startTime(), cmd.endTime());

        return SingleReservation.builder()
            .applicant(applicant)
            .reservable(reservable)
            .date(cmd.date())
            .startTime(cmd.startTime())
            .endTime(cmd.endTime())
            .status(Status.PENDING)
            .build();
    }

    private RecurringReservation createRecurring(
            CreateReservationCommand cmd, Applicant applicant, Reservable reservable) {

        // Calcular todas las fechas de la serie
        List<LocalDate> occurrenceDates = generateOccurrenceDates(cmd);

        // Overlap check por cada fecha
        List<LocalDate> conflictingDates = occurrenceDates.stream()
            .filter(date -> hasOverlapOnDate(cmd.reservableId(), date, cmd.startTime(), cmd.endTime()))
            .toList();

        if (!conflictingDates.isEmpty())
            throw new ReservationOverlapException(conflictingDates);

        // Construir recurrences
        List<ReservationRecurrence> recurrences = buildRecurrences(cmd);

        return RecurringReservation.builder()
            .applicant(applicant)
            .reservable(reservable)
            .seriesDateFrom(cmd.seriesDateFrom())
            .seriesDateTo(cmd.seriesDateTo())
            .startTime(cmd.startTime())
            .endTime(cmd.endTime())
            .frequency(cmd.frequency())
            .recurrences(recurrences)
            .status(Status.PENDING)
            .build();
    }

    // -------------------------
    // Validaciones
    // -------------------------

    private void validateAdvanceTime(Reservable reservable,
            CreateReservationCommand cmd, Clock clock) {

        LocalDate firstDate = cmd.recurrenceType() == RecurrenceType.SINGLE
            ? cmd.date()
            : cmd.seriesDateFrom();

        LocalDateTime firstOccurrence = firstDate.atTime(cmd.startTime());
        LocalDateTime now = LocalDateTime.now(clock);
        long hoursUntil = ChronoUnit.HOURS.between(now, firstOccurrence);

        if (hoursUntil < reservable.getMinAdvanceHours())
            throw new InsufficientAdvanceTimeException(
                reservable.getMinAdvanceHours(), hoursUntil);
    }

    private void validateStudentRestrictions(
            Applicant applicant, Reservable reservable, CreateReservationCommand cmd) {

        if (applicant.isStudent() && reservable.isRestrictedForStudents())
            throw new ResourceNotAvailableForStudentsException(reservable.getId());

        if (applicant.isStudent() && cmd.attendees() != null
                && cmd.attendees() > reservable.getCapacity())
            throw new CapacityExceededException(reservable.getCapacity(), cmd.attendees());
    }

    private void validateSeriesDuration(CreateReservationCommand cmd) {
        if (cmd.recurrenceType() == RecurrenceType.SINGLE) return;

        long weeks = ChronoUnit.WEEKS.between(cmd.seriesDateFrom(), cmd.seriesDateTo());
        if (weeks > maxWeeks)
            throw new RecurrenceTooLongException(maxWeeks, weeks);
    }

    // -------------------------
    // Overlap check
    // -------------------------

    private void validateNoSingleOverlap(
            Long reservableId, LocalDate date, LocalTime startTime, LocalTime endTime) {

        if (hasOverlapOnDate(reservableId, date, startTime, endTime))
            throw new ReservationOverlapException(List.of(date));
    }

    private boolean hasOverlapOnDate(
            Long reservableId, LocalDate date, LocalTime startTime, LocalTime endTime) {

        // Verificar contra SingleReservations
        boolean singleConflict = reservationRepository.existsSingleOverlap(
            reservableId, date, startTime, endTime, List.of(Status.PENDING, Status.APPROVED));

        if (singleConflict) return true;

        // Verificar contra RecurringReservations activas ese día
        List<RecurringReservation> activeSeries = reservationRepository
            .findActiveRecurringByReservableAndDate(
                reservableId, date, date.getDayOfWeek());

        return activeSeries.stream().anyMatch(series -> {
            // Verificar que esa fecha no esté excluida
            boolean isExcluded = series.getExceptions().stream()
                .anyMatch(e -> e.getExcludedDate().equals(date));

            if (isExcluded) return false;

            // Verificar solapamiento de horario
            return series.getStartTime().isBefore(endTime)
                && series.getEndTime().isAfter(startTime);
        });
    }

    // -------------------------
    // Generación de fechas
    // -------------------------

    private List<LocalDate> generateOccurrenceDates(CreateReservationCommand cmd) {
        return switch (cmd.frequency()) {
            case WEEKLY -> generateWeeklyDates(
                cmd.daysOfWeek(), cmd.seriesDateFrom(), cmd.seriesDateTo(), 1);
            case BIWEEKLY -> generateWeeklyDates(
                cmd.daysOfWeek(), cmd.seriesDateFrom(), cmd.seriesDateTo(), 2);
            case MONTHLY -> generateNthMonthDates(cmd.seriesDateFrom(), cmd.seriesDateTo(), 1);
            case BIMONTHLY -> generateNthMonthDates(cmd.seriesDateFrom(), cmd.seriesDateTo(), 2);
            case QUARTERLY -> generateNthMonthDates(cmd.seriesDateFrom(), cmd.seriesDateTo(), 3);
            case SEMIANNUALLY -> generateNthMonthDates(cmd.seriesDateFrom(), cmd.seriesDateTo(), 6);
            case ANNUALLY -> generateNthMonthDates(cmd.seriesDateFrom(), cmd.seriesDateTo(), 12);
        };
    }

    private List<LocalDate> generateWeeklyDates(
            List<DayOfWeek> days, LocalDate from, LocalDate to, int weekStep) {

        Set<DayOfWeek> daySet = new HashSet<>(days);
        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor = from;

        while (!cursor.isAfter(to)) {
            if (daySet.contains(cursor.getDayOfWeek())) {
                dates.add(cursor);
            }
            cursor = cursor.plusDays(1);

            if (weekStep == 2
                    && cursor.getDayOfWeek() == DayOfWeek.MONDAY
                    && isOddWeek(cursor, from)) {
                cursor = cursor.plusWeeks(1);
            }
        }
        return dates;
    }

    private List<LocalDate> generateNthMonthDates(
            LocalDate from, LocalDate to, int monthStep) {

        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor = from;

        while (!cursor.isAfter(to)) {
            dates.add(cursor);
            cursor = from.plusMonths((long) monthStep * dates.size());
        }
        return dates;
    }

    private boolean isOddWeek(LocalDate date, LocalDate seriesStart) {
        long weeks = ChronoUnit.WEEKS.between(
            seriesStart.with(DayOfWeek.MONDAY),
            date.with(DayOfWeek.MONDAY)
        );
        return weeks % 2 != 0;
    }

    private List<ReservationRecurrence> buildRecurrences(CreateReservationCommand cmd) {
        if (cmd.daysOfWeek() == null || cmd.daysOfWeek().isEmpty()) return List.of();

        return cmd.daysOfWeek().stream()
            .map(day -> ReservationRecurrence.builder()
                .dayOfWeek(day)
                .build())
            .toList();
    }

    // -------------------------
    // Notificaciones
    // -------------------------

    @Async
    protected void notifyAdmins(Reservation reservation) {
        // Aquí llamas a tu SendNotificationUseCase con NotificationType.RESERVATION_CREATED
        sendNotificationUseCase.notifyAllAdmins(
            new SendNotificationCommand(NotificationType.RESERVATION_CREATED, reservation.getId())
        );
    }
}