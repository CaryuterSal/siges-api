package dev.spiffocode.sigesapi.auth.domain.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class InvalidCredentialsException extends AuthenticationException {

    private final int remainingAttempts;

    public InvalidCredentialsException(AuthenticationException e, int remaining) {
        super(e.getMessage(), e);
        remainingAttempts = remaining;
    }
}
