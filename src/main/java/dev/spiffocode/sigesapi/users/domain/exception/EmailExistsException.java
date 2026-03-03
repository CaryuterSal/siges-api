package dev.spiffocode.sigesapi.users.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

public class EmailExistsException extends ConflictingStateException {
    public EmailExistsException(String email) {
        super("Email '" + email + "' already exists");
    }
}
