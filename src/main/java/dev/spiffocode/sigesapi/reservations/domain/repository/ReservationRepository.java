package dev.spiffocode.sigesapi.reservations.domain.repository;

import dev.spiffocode.sigesapi.reservations.domain.model.RecurringReservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.SingleReservation;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<@NonNull Reservation,@NonNull Long> {

    @Query("""
        SELECT r FROM RecurringReservation r
        WHERE r.reservable.id = :reservableId
        AND r.status IN ('APPROVED', 'PENDING')
        AND r.seriesDateFrom <= :to
        AND r.seriesDateTo >= :from
    """)
    List<RecurringReservation> findActiveRecurringByReservableAndDateRange(
        @Param("reservableId") Long reservableId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    @Query("""
        SELECT r FROM SingleReservation r
        WHERE r.reservable.id = :reservableId
        AND r.status IN ('APPROVED', 'PENDING')
        AND r.date BETWEEN :from AND :to
    """)
    List<SingleReservation> findActiveSingleByReservableAndDateRange(
        @Param("reservableId") Long reservableId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );
}