package dev.spiffocode.sigesapi.reservations.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository
                extends JpaRepository<@NonNull Reservation, @NonNull Long>,
                JpaSpecificationExecutor<@NonNull Reservation> {

        @Query("""
                            SELECT COUNT(r) > 0 FROM Reservation r
                            WHERE r.reservable = :reservable
                            AND r.status IN :statuses
                            AND r.date = :date
                            AND r.startTime < :endTime
                            AND r.endTime > :startTime
                        """)
        boolean existsOverlap(
                        @Param("reservable") Reservable reservable,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime,
                        @Param("statuses") List<Status> statuses);

        @Query("""
                            SELECT COUNT(r) > 0 FROM Reservation r
                            WHERE r.reservable = :reservable
                            AND r.status IN :statuses
                            AND r.date = :date
                            AND r.startTime < :endTime
                            AND r.endTime > :startTime
                            AND r.id <> :id
                        """)
        boolean existsOverlapExcluding(
                        @Param("reservable") Reservable reservable,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime,
                        @Param("statuses") List<Status> statuses,
                        @Param("id") Long id);

        List<Reservation> findByReservableAndDateBetweenAndStatusIn(
                        Reservable reservable, LocalDate from, LocalDate to, List<Status> statuses);

        @Query("""
                            SELECT r FROM Reservation r
                            WHERE r.reservable = :reservable
                            AND r.status IN :statuses
                            AND r.date = :date
                            AND r.startTime < :endTime
                            AND r.endTime > :startTime
                        """)
        List<Reservation> findOverlappingReservations(
                        @Param("reservable") Reservable reservable,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime,
                        @Param("statuses") List<Status> statuses);

        @Query("""
                            SELECT r FROM Reservation r
                            WHERE r.reservable = :reservable
                            AND r.status IN :statuses
                            AND r.date = :date
                            AND r.startTime < :endTime
                            AND r.endTime > :startTime
                            AND r.id <> :excludeId
                        """)
        List<Reservation> findOverlappingReservationsExcluding(
                        @Param("reservable") Reservable reservable,
                        @Param("date") LocalDate date,
                        @Param("startTime") LocalTime startTime,
                        @Param("endTime") LocalTime endTime,
                        @Param("statuses") List<Status> statuses,
                        @Param("excludeId") Long excludeId);

        long countByPetitionerIdAndReturnedLateTrue(Long petitionerId);

        List<Reservation> findByStatusAndDateAndStartTimeLessThanEqual(
                        Status status, LocalDate date, LocalTime startTime);

        List<Reservation> findByStatusAndDateAndEndTimeLessThanEqual(
                        Status status, LocalDate date, LocalTime endTime);
}