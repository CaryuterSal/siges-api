package dev.spiffocode.sigesapi.config;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import dev.spiffocode.sigesapi.common.exceptions.AccessDeniedException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@Hidden
@RestControllerAdvice
@Order(10)
public class SecurityControllerAdvice {

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail authentication() {
        return unauthorized("Invalid credentials");
    }

    @ExceptionHandler(TokenExpiredException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail expired() {
        return unauthorized("Token expired");
    }

    @ExceptionHandler({JWTVerificationException.class, JWTDecodeException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ProblemDetail invalid() {
        return unauthorized("Invalid token");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ProblemDetail forbidden() {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Access denied");
        return pd;
    }

    private ProblemDetail unauthorized(String message) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle(message);
        pd.setType(URI.create("https://api.siges.dev/errors/auth"));
        return pd;
    }
}
