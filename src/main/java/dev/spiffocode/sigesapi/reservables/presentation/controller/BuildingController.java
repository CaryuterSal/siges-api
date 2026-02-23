package dev.spiffocode.sigesapi.reservables.presentation.controller;

import dev.spiffocode.sigesapi.common.presentation.ValidationProblem;
import dev.spiffocode.sigesapi.reservables.application.service.BuildingService;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/buildings", version = "1.0.0")
@RequiredArgsConstructor
@Tag(name = "buildings", description = "Endpoints for managing buildings")
@SecurityRequirement(name = "jwt")
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get a building by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Building found", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "Building not found")
    })
    public BuildingDto getBuilding(@PathVariable long id) {
        return buildingService.getBuilding(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get all buildings")
    public List<BuildingDto> getAllBuildings(
            @RequestParam(name = "onlyActive", defaultValue = "true") boolean onlyActive) {
        return buildingService.getAllBuildings(onlyActive);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new building")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Building registered", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class)))
    })
    public BuildingDto registerBuilding(@RequestBody @Valid BuildingRegisterDto request) {
        return buildingService.registerBuilding(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing building")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Building updated", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
            @ApiResponse(responseCode = "404", description = "Building not found")
    })
    public BuildingDto updateBuilding(@PathVariable long id, @RequestBody @Valid BuildingUpdateDto request) {
        return buildingService.updateBuilding(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a building")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Building deactivated"),
            @ApiResponse(responseCode = "404", description = "Building not found")
    })
    public ResponseEntity<Void> deactivateBuilding(@PathVariable long id) {
        buildingService.deactivateBuilding(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a building")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Building activated"),
            @ApiResponse(responseCode = "404", description = "Building not found")
    })
    public ResponseEntity<Void> activateBuilding(@PathVariable long id) {
        buildingService.activateBuilding(id);
        return ResponseEntity.noContent().build();
    }
}
