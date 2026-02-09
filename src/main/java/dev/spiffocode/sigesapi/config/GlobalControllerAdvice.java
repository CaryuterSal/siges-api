package dev.spiffocode.sigesapi.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import dev.spiffocode.sigesapi.common.exceptions.AccessDeniedException;
import lombok.NonNull;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice
public class GlobalControllerAdvice extends ResponseEntityExceptionHandler {


    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {

        ProblemDetail pd =
                ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);

        pd.setType(URI.create("https://api.siges.dev/errors/auth/invalid-credentials"));
        pd.setTitle("Authentication failed");
        pd.setDetail("Invalid username or password");

        return pd;
    }


    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail badCredentials() {
        return unauthorized("Invalid credentials");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ProblemDetail forbidden() {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Access denied");
        return pd;
    }

    @ExceptionHandler(TokenExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail expired() {
        return unauthorized("Token expired");
    }

    @ExceptionHandler(JWTVerificationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail invalidToken() {
        return unauthorized("Invalid token");
    }

    @Override
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<@NonNull Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        ProblemDetail pd =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        pd.setTitle("Validation failed");

        pd.setProperty("errors",
                ex.getBindingResult().getFieldErrors()
                        .stream()
                        .map(f -> f.getField() + ": " + f.getDefaultMessage())
                        .toList());

        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail unknown(Exception ex) {

        ProblemDetail pd =
                ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        pd.setTitle("Unexpected error");
        return pd;
    }


    private ProblemDetail unauthorized(String message) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle(message);
        return pd;
    }
}
