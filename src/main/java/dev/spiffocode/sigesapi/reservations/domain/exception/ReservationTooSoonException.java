package dev.spiffocode.sigesapi.reservations.domain.exception;

import java.time.Duration;

public class ReservationTooSoonException extends RuntimeException {
    public ReservationTooSoonException(Long Id, Duration minimumBookInAdvance) {
        super("Reservable with Id %dl Not Available. Should be booked with at least %s in advance".formatted(Id, minimumBookInAdvance.toString()));
    }
}
