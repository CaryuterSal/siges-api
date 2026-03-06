package dev.spiffocode.sigesapi.reservations.domain.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Getter
@Table(name = "single_reservations")
@DiscriminatorValue("SINGLE")
public class SingleReservation extends Reservation {

    @NotNull
    @FutureOrPresent
    private LocalDate date;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;


    @Override
    public void approve(Clock clock) {
        if (this.getStatus() != Status.PENDING)
            throw new IllegalStateException("Solo se puede aprobar una reservación PENDING");
        this.setStatus(Status.APPROVED);
        this.setApprovedAt(LocalDateTime.now(clock));
    }

    @Override
    public void reject(Clock clock, String reason) {
        if (this.getStatus() != Status.PENDING)
            throw new IllegalStateException("Solo se puede aprobar una reservación PENDING");
        this.setStatus(Status.REJECTED);
        this.setApprovedAt(LocalDateTime.now(clock));
    }

    @Override
    public void cancel(Clock clock, String reason) {
        if (this.getStatus() == Status.FINISHED || this.getStatus() == Status.CANCELLED || this.getStatus() == Status.REJECTED)
            throw new IllegalStateException("No se puede cancelar en estado " + this.getStatus());
        this.setStatus(Status.CANCELLED);
        this.setApprovedAt(LocalDateTime.now(clock));
    }
}
