package dev.spiffocode.sigesapi.reservations.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationOverlapException extends ConflictingStateException {
    public ReservationOverlapException(LocalDate date, LocalTime from, LocalTime to) {
        super("Ya existe una reservación para el %s de %s a %s".formatted(date.toString(), from.toString(), to.toString()));
    }
}
