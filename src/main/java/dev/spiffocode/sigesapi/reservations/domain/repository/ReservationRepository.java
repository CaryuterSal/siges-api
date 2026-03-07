package dev.spiffocode.sigesapi.reservations.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<@NonNull Reservation, @NonNull Long>, JpaSpecificationExecutor<@NonNull Reservation> {

    @Query("""
        SELECT COUNT(r) > 0 FROM Reservation r
        WHERE r.reservable = :reservableId
        AND r.status IN :statuses
        AND r.date = :date
        AND r.startTime < :endTime
        AND r.endTime > :startTime
    """)
    boolean existsOverlap(
            Long reservableId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            List<Status> statuses
    );

    List<Reservation> findByReservableAndDateRangeAndStatusIn(
            Reservable reservable, LocalDate from, LocalDate to, List<Status> statuses
    );
}