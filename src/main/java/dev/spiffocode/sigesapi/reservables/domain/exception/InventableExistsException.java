package dev.spiffocode.sigesapi.reservables.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

public class InventableExistsException extends ConflictingStateException {
    public InventableExistsException(String message) {
        super(message);
    }
}
