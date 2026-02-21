package dev.spiffocode.sigesapi.common.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.util.List;

@Schema(name = "ValidationProblem")
public class ValidationProblem extends ProblemDetail {

    @Schema(
        description = "List of validation errors",
        example = """
        [
          "email: must be a well-formed email address",
          "password: size must be between 8 and 64"
        ]
        """
    )
    public List<String> errors;

    public ValidationProblem(ProblemDetail pd) {
        super(pd);
    }

    public static ValidationProblem forStatus(HttpStatus status) {
        return new ValidationProblem(ProblemDetail.forStatus(status));
    }
}
