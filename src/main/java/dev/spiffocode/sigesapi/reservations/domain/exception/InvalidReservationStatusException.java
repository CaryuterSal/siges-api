package dev.spiffocode.sigesapi.reservations.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;

public class InvalidReservationStatusException extends ConflictingStateException {
    public InvalidReservationStatusException(Status currentStatus, Status desiredStatus) {
        super("Invalid reservation status change. Can't change from " + currentStatus + " to " + desiredStatus);
    }
}
