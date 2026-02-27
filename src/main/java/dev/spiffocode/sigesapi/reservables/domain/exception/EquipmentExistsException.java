package dev.spiffocode.sigesapi.reservables.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

public class EquipmentExistsException extends ConflictingStateException {
    public EquipmentExistsException(String message) {
        super(message);
    }
}
