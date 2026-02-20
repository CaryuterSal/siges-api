package dev.spiffocode.sigesapi.auth.domain.exception;

public class AccountTemporarilyLockedException extends RuntimeException {
    public AccountTemporarilyLockedException(String message) {
        super(message);
    }
}