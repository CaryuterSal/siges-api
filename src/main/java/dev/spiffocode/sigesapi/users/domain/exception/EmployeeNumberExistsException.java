package dev.spiffocode.sigesapi.users.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

public class EmployeeNumberExistsException extends ConflictingStateException {
    public EmployeeNumberExistsException(String employeeNumber) {
        super("Employee number '" + employeeNumber + "' already exists");
    }
}
