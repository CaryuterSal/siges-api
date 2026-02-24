package dev.spiffocode.sigesapi.common.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public class InvalidCredentialsProblem extends ProblemDetail {

    @Schema(
            description = "Remaining log in attempts, being 1 the last available attempt before account being blocked",
            example = "2"
    )
    public Integer remainingAttempts;

    public InvalidCredentialsProblem(ProblemDetail pd) {
        super(pd);
    }

    public static InvalidCredentialsProblem forStatus(HttpStatus status) {
        return new InvalidCredentialsProblem(ProblemDetail.forStatus(status));
    }
}
