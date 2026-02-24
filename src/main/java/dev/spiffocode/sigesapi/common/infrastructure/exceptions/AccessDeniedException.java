package dev.spiffocode.sigesapi.common.infrastructure.exceptions;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
