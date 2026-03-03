package dev.spiffocode.sigesapi.users.domain.exception;

public class InvalidRecoveryTokenException extends RuntimeException {
    public InvalidRecoveryTokenException(String message) {
        super(message);
    }
}
