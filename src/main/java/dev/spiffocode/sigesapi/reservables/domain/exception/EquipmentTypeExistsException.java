package dev.spiffocode.sigesapi.reservables.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

public class EquipmentTypeExistsException extends ConflictingStateException {
    public EquipmentTypeExistsException(String message) {
        super(message);
    }
}
