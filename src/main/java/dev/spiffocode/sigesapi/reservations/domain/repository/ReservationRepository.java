package dev.spiffocode.sigesapi.reservations.domain.repository;

import dev.spiffocode.sigesapi.reservations.domain.model.RecurringReservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.SingleReservation;

import java.util.List;

// domain/repository/ReservationRepository.java
public interface ReservationRepository {

    Optional<Reservation> findById(Long id);
    Reservation save(Reservation reservation);

    // Calendario (ya lo usas en GetReservableCalendarUseCase)
    List<RecurringReservation> findActiveRecurringByReservableAndDateRange(
        Long reservableId, LocalDate from, LocalDate to
    );
    List<SingleReservation> findActiveSingleByReservableAndDateRange(
        Long reservableId, LocalDate from, LocalDate to
    );

    // Overlap check — single
    boolean existsSingleOverlap(
        Long reservableId, LocalDate date, LocalTime startTime, LocalTime endTime, List<Status> activeStatuses
    );

    // Overlap check — series recurrentes activas que aplican en un día de semana
    List<RecurringReservation> findActiveRecurringByReservableAndDate(
        Long reservableId, LocalDate date, DayOfWeek dayOfWeek
    );

    // Historial
    Page<Reservation> findByApplicantAndStatusIn(
        Long applicantId, List<Status> statuses, Pageable pageable
    );

    // Para admins: todas las reservaciones pendientes
    Page<Reservation> findByStatusIn(List<Status> statuses, Pageable pageable);
}