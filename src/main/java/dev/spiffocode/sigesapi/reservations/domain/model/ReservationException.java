package dev.spiffocode.sigesapi.reservations.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Entity
@Table(name = "reservation_exceptions")
public class ReservationException {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private RecurringReservation reservation;

    @NotNull
    private LocalDate excludedDate;

    @Enumerated(EnumType.STRING)
    private ExceptionType type;

    @OneToOne
    private SingleReservation replacement;
}