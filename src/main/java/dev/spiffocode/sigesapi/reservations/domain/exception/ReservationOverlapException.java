package dev.spiffocode.sigesapi.reservations.domain.exception;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationOverlapException extends RuntimeException {
    public ReservationOverlapException(LocalDate date, LocalTime from, LocalTime to) {
        super("Ya existe una reservación para el %s de %s a %s".formatted(date.toString(), from.toString(), to.toString()));
    }
}
