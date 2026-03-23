package dev.spiffocode.sigesapi.common.infrastructure.config;

import dev.spiffocode.sigesapi.common.presentation.ValidationProblem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.List;

@Slf4j
@RestControllerAdvice
@Order
public class GlobalControllerAdvice extends ResponseEntityExceptionHandler {

    /**
     * 400 – Jakarta validation
     * Aplica a TODOS los endpoints
     */
    @Override
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(content = @Content(schema = @Schema(implementation = ValidationProblem.class)), responseCode = "400")
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


    @Override

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(content = @Content(schema = @Schema(implementation = ValidationProblem.class)), responseCode = "400")
    protected ResponseEntity<@NonNull Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        ValidationProblem pd = ValidationProblem.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Malformed request body");

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String field = ife.getPath().isEmpty() ? "unknown"
                    : ife.getPath().getLast().getPropertyName();
            String value = String.valueOf(ife.getValue());
            String type  = ife.getTargetType() != null
                    ? ife.getTargetType().getSimpleName()
                    : "unknown";

            pd.setProperty("errors", List.of(
                    field + ": valor '" + value + "' no es un " + type + " válido"
            ));
        } else {
            pd.setProperty("errors", List.of(ex.getMessage()));
        }

        return ResponseEntity.badRequest().body(pd);
    }

    /**
     * 500 – fallback universal
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail unknown(Exception ex) {
        log.warn(ex.getMessage(), ex);
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Unexpected error");
        return pd;
    }
}
