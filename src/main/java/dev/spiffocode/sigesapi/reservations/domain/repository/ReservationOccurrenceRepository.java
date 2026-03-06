package dev.spiffocode.sigesapi.reservations.domain.repository;

import dev.spiffocode.sigesapi.reservations.domain.model.RecurringReservation;
import dev.spiffocode.sigesapi.reservations.domain.model.ReservationOccurrence;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

// domain/repository/ReservationOccurrenceRepository.java
public interface ReservationOccurrenceRepository {
    ReservationOccurrence save(ReservationOccurrence occurrence);
    List<ReservationOccurrence> findByReservationAndOccurrenceDateBefore(
            RecurringReservation reservation, LocalDate date
    );
    // Para el scheduler
    List<RecurringReservation> findApprovedRecurringWithOccurrenceOn(LocalDate date);

    // infrastructure/persistence/ReservationOccurrenceJpaRepository.java
    @Query("""
    SELECT r FROM RecurringReservation r
    JOIN r.recurrences rec
    WHERE r.status = 'APPROVED'
    AND r.seriesDateFrom <= :date
    AND r.seriesDateTo >= :date
    AND rec.dayOfWeek = :dayOfWeek
    AND NOT EXISTS (
        SELECT o FROM ReservationOccurrence o
        WHERE o.reservation = r
        AND o.occurrenceDate = :date
    )
""")
    List<RecurringReservation> findApprovedRecurringWithOccurrenceOn(
            @Param("date") LocalDate date,
            @Param("dayOfWeek") DayOfWeek dayOfWeek
    );
}