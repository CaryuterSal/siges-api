package dev.spiffocode.sigesapi.reservables.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.NotFoundException;

public class SpaceTypeNotFound extends NotFoundException {

    public SpaceTypeNotFound(String message, long id) {
        super(message, id);
    }
}
