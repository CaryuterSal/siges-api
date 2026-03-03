package dev.spiffocode.sigesapi.users.infrastructure.controller;

import dev.spiffocode.sigesapi.common.presentation.ValidationProblem;
import dev.spiffocode.sigesapi.users.application.service.UserRegistrationService;
import dev.spiffocode.sigesapi.users.presentation.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(version = "1.0.0")
@RequiredArgsConstructor
@Tag(name = "User Registration", description = "Endpoints for Admins to register other users in the system")
public class UserRegistrationController {

    private final UserRegistrationService registrationService;

    @PostMapping("/admins")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Location", description = "Relative URL location of the newly created admin")),
            @ApiResponse(responseCode = "409", description = "email or phone number is already in use"),
            @ApiResponse(responseCode = "400", description = "Validation problem",  content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
    })
    @Operation(summary = "Registers a new admin")
    public ResponseEntity<@NonNull AdminResponse> registerAdmin(@RequestBody @Valid AdminRegistrationRequest request){
        AdminResponse response = registrationService.registerAdmin(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/users/{id}")
                .queryParam("type", "ID")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/students")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Location", description = "Relative URL location of the newly created student")),
            @ApiResponse(responseCode = "409", description = "email, phone number or student registration number is already in use"),
            @ApiResponse(responseCode = "400", description = "Validation problem",  content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
    })
    @Operation(summary = "Registers a new admin")
    public ResponseEntity<@NonNull StudentResponse> registerStudent(@RequestBody @Valid StudentRegistrationRequest request){
        StudentResponse response = registrationService.registerStudent(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/users/{id}")
                .queryParam("type", "ID")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/institutional-staff")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    useReturnTypeSchema = true,
                    headers = @Header(name = "Location", description = "Relative URL location of the newly created institutional staff")),
            @ApiResponse(responseCode = "409", description = "email, phone number or employee number is already in use"),
            @ApiResponse(responseCode = "400", description = "Validation problem",  content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
    })
    @Operation(summary = "Registers a new institutional staff")
    public ResponseEntity<@NonNull InstitutionalStaffResponse> registerInstitutionalStaff(@RequestBody @Valid InstitutionalStaffRegistrationRequest request){
        InstitutionalStaffResponse response = registrationService.registerInstitutionalStaff(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/users/{id}")
                .queryParam("type", "ID")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }
}
