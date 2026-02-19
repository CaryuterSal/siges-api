package dev.spiffocode.sigesapi.common.infrastructure.config;

import lombok.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Order
public class GlobalControllerAdvice extends ResponseEntityExceptionHandler {

    /**
     * 400 – Jakarta validation
     * Aplica a TODOS los endpoints
     */
    @Override
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<@NonNull Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        ValidationProblem pd = ValidationProblem.forStatus(HttpStatus.BAD_REQUEST);

        pd.setTitle("Validation failed");
        pd.setProperty("errors",
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(f -> f.getField() + ": " + f.getDefaultMessage())
                        .toList());

        return ResponseEntity.badRequest().body(pd);
    }

    /**
     * 429 – Rate limiting
     */
    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ProblemDetail rateLimit() {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.TOO_MANY_REQUESTS);
        pd.setTitle("Too many requests");
        return pd;
    }

    /**
     * 500 – fallback universal
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail unknown(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Unexpected error");
        return pd;
    }
}
