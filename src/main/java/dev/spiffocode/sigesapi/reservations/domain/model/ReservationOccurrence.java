package dev.spiffocode.sigesapi.reservations.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "reservation_occurrences",
    indexes = {
        @Index(columnList = "reservation_id, occurrence_date")
    }
)
public class ReservationOccurrence {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private RecurringReservation reservation;

    @NotNull
    private LocalDate occurrenceDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private OccurrenceStatus status;

    private LocalDateTime materializedAt;
}