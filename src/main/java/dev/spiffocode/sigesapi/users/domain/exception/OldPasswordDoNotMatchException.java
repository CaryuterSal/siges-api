package dev.spiffocode.sigesapi.users.domain.exception;

public class OldPasswordDoNotMatchException extends RuntimeException {
    public OldPasswordDoNotMatchException() {
        super("Old password do not match with current user password");
    }
}
