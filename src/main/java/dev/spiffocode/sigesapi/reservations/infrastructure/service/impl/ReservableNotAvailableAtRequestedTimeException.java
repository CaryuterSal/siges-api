package dev.spiffocode.sigesapi.reservations.infrastructure.service.impl;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservableNotAvailableAtRequestedTimeException extends ConflictingStateException {
    public ReservableNotAvailableAtRequestedTimeException(Long id, LocalDate date, LocalTime startTime, LocalTime endTime) {
        super("Reservation with ID %dl does not have an availability for date: %s that covers from %s to %s".formatted(id, date, startTime.toString(), endTime.toString()));
    }
}
