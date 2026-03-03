package dev.spiffocode.sigesapi.users.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long id) {
        super("User with id %s not found".formatted(id), id.toString());
    }

    public UserNotFoundException(String identifier) {
        super("User with identifier %s not found".formatted(identifier), identifier);
    }
}
