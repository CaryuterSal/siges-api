package dev.spiffocode.sigesapi.common.infrastructure.exceptions;

public class ConflictingStateException extends RuntimeException {
    public ConflictingStateException(String message) {
        super(message);
    }
}
