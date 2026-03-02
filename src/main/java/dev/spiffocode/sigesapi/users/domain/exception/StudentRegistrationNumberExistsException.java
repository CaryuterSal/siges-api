package dev.spiffocode.sigesapi.users.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

public class StudentRegistrationNumberExistsException extends ConflictingStateException {
    public StudentRegistrationNumberExistsException(String regNumber) {
        super("Student registration number '" + regNumber + "' already exists");
    }
}

