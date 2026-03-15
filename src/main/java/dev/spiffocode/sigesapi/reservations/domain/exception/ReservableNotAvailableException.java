package dev.spiffocode.sigesapi.reservations.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

public class ReservableNotAvailableException extends ConflictingStateException {
    public ReservableNotAvailableException(Long id) {
        super("Reservable with Id %dl Not Available".formatted(id));
    }
}
