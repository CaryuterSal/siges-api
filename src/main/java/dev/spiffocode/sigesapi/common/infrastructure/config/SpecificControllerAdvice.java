package dev.spiffocode.sigesapi.common.infrastructure.config;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(20)
@RequiredArgsConstructor
public class SpecificControllerAdvice {

    @ExceptionHandler(ConflictingStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail conflictingState(ConflictingStateException e) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle(e.getMessage());
        pd.setDetail(e.getMessage());
        return pd;
    }

}
