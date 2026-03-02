package dev.spiffocode.sigesapi.users.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

public class PhoneNumberExistsException extends ConflictingStateException {
    public PhoneNumberExistsException(String phoneNumber) {
        super("Phone number '" + phoneNumber + "' already exists");
    }
}
