package dev.spiffocode.sigesapi.reservables.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

public class SpaceTypeExistsException extends ConflictingStateException {
    public SpaceTypeExistsException(String message) {
        super(message);
    }
}
