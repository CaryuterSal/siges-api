package dev.spiffocode.sigesapi.reservations.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservableHasAvailabilityExceptionException extends ConflictingStateException {
    public ReservableHasAvailabilityExceptionException(Long id, LocalDate date, LocalTime startTime, LocalTime endTime) {
        super("Reservation with ID %dl has an availability exception for date: %s that overlaps with desired schedule: %s to %s".formatted(id, date, startTime.toString(), endTime.toString()));
    }
}
