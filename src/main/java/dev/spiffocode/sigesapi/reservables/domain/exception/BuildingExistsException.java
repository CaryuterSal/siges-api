package dev.spiffocode.sigesapi.reservables.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

public class BuildingExistsException extends ConflictingStateException {
    public BuildingExistsException(String message) {
        super(message);
    }
}
