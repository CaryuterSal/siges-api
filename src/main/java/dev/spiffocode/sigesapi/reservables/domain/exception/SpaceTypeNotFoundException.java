package dev.spiffocode.sigesapi.reservables.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.NotFoundException;

public class SpaceTypeNotFoundException extends NotFoundException {

    public SpaceTypeNotFoundException(String message, long id) {
        super(message, id);
    }
}
