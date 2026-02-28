package dev.spiffocode.sigesapi.reservables.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

public class SpaceExistsException extends ConflictingStateException {
    public SpaceExistsException(String message) {
        super(message);
    }
}
