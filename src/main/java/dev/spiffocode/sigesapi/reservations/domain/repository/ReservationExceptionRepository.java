package dev.spiffocode.sigesapi.reservations.domain.repository;

// domain/repository/ReservationExceptionRepository.java
public interface ReservationExceptionRepository {
    ReservationException save(ReservationException exception);
    List<ReservationException> findByReservation(RecurringReservation reservation);
    Optional<ReservationException> findByReservationAndExcludedDate(
        RecurringReservation reservation, LocalDate date
    );
}