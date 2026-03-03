package dev.spiffocode.sigesapi.users.domain.exception;

public class RecoveryTokenExpiredException extends RuntimeException {
    public RecoveryTokenExpiredException(String message) {
        super(message);
    }
}
