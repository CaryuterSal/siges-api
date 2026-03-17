package dev.spiffocode.sigesapi.common.infrastructure.config;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;
import dev.spiffocode.sigesapi.common.infrastructure.exceptions.NotFoundException;
import dev.spiffocode.sigesapi.common.infrastructure.exceptions.StorageException;
import dev.spiffocode.sigesapi.reservations.domain.exception.ReservableNotAvailableForStudentsException;
import dev.spiffocode.sigesapi.reservations.domain.exception.ReservationTooSoonException;
import dev.spiffocode.sigesapi.users.domain.exception.InvalidRecoveryTokenException;
import dev.spiffocode.sigesapi.users.domain.exception.RecoveryTokenExpiredException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(20)
@Hidden
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

    @ExceptionHandler(PropertyReferenceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail propertyReference(PropertyReferenceException e) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Invalid sort field");
        pd.setDetail(e.getMessage());
        return pd;
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail notFound(NotFoundException e) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Not found");
        pd.setDetail(e.getMessage());
        pd.setProperty("id", e.getId());
        return pd;
    }

    @ExceptionHandler(InvalidRecoveryTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleInvalidRecoveryToken(InvalidRecoveryTokenException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Invalid recovery token");
        return problem;
    }

    @ExceptionHandler(RecoveryTokenExpiredException.class)
    @ResponseStatus(HttpStatus.GONE)
    public ProblemDetail handleRecoveryTokenExpired(RecoveryTokenExpiredException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.GONE, ex.getMessage());
        problem.setTitle("Recovery token expired or already used");
        return problem;
    }

    @ExceptionHandler({ReservableNotAvailableForStudentsException.class, ReservationTooSoonException.class})
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ProblemDetail handleUnprocessableContent(Exception e){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
        problem.setTitle("Business Rule violation");
        return problem;
    }

    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Invalid request");
        return problem;
    }

    @ExceptionHandler({StorageException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ProblemDetail handleStorageException(StorageException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        problem.setTitle("Internal Storage error");
        return problem;
    }
}
