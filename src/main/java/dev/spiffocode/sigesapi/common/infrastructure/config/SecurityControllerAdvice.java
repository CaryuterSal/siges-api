package dev.spiffocode.sigesapi.common.infrastructure.config;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import dev.spiffocode.sigesapi.auth.domain.exception.AccountTemporarilyLockedException;
import dev.spiffocode.sigesapi.auth.domain.exception.InvalidCredentialsException;
import dev.spiffocode.sigesapi.auth.domain.exception.JwtBlacklistedException;
import dev.spiffocode.sigesapi.auth.infrastructure.LogInAttemptsProperties;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@Hidden
@RestControllerAdvice
@Order(10)
@RequiredArgsConstructor
public class SecurityControllerAdvice {

    private final LogInAttemptsProperties loginAttemptsProperties;

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail authentication() {
        return unauthorized("Invalid credentials");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail authentication(InvalidCredentialsException e) {
        ProblemDetail pd = unauthorized("Invalid credentials");
        pd.setProperty("remainingAttempts", e.getRemainingAttempts());
        return pd;
    }

    @ExceptionHandler(TokenExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail expired() {
        return unauthorized("Token expired");
    }

    @ExceptionHandler({JWTVerificationException.class, JWTDecodeException.class, JwtBlacklistedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail invalid() {
        return unauthorized("Invalid token");
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ProblemDetail forbidden() {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Access denied");
        return pd;
    }

    @ExceptionHandler(AccountTemporarilyLockedException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ResponseEntity<@NonNull ProblemDetail> tooManyLoginAttempts() {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
        pd.setTitle("Too many login attempts");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(TimeUnit.MINUTES.toSeconds(loginAttemptsProperties.getLockMinutes())))
                .body(pd);
    }

    private ProblemDetail unauthorized(String message) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle(message);
        pd.setType(URI.create("https://api.siges.dev/errors/auth"));
        return pd;
    }
}
