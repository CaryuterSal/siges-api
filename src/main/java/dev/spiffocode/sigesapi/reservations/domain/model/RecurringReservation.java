package dev.spiffocode.sigesapi.reservations.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Getter
@Table(name = "recurring_reservations")
@DiscriminatorValue("RECURRING")
public class RecurringReservation extends Reservation{


    @NotNull
    private LocalDate seriesDateFrom;

    @NotNull
    private LocalDate seriesDateTo;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RecurrenceFrequency frequency;

    // Para WEEKLY/BIWEEKLY: días específicos de la semana
    @Builder.Default
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<ReservationRecurrence> recurrences = new ArrayList<>();

    // Solo excepciones: cancelaciones individuales + ocurrencias modificadas
    @Builder.Default
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<ReservationException> exceptions = new ArrayList<>();

    // Ocurrencias pasadas materializadas como historial
    @Builder.Default
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<ReservationOccurrence> pastOccurrences = new ArrayList<>();

    @Override
    public void cancel(Clock clock, String reason) {

    }

    @Override
    public void reject(Clock clock, String reason) {

    }

    @Override
    public void approve(Clock clock) {

    }
}
