package dev.spiffocode.sigesapi.reservables.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.NotFoundException;

public class EquipmentTypeNotFoundException extends NotFoundException {
    public EquipmentTypeNotFoundException(String message, long id) {
        super(message, String.valueOf(id));
    }
}
