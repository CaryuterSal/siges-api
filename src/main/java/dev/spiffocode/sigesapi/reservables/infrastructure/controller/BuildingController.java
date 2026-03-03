package dev.spiffocode.sigesapi.reservables.infrastructure.controller;

import dev.spiffocode.sigesapi.common.presentation.ValidationProblem;
import dev.spiffocode.sigesapi.reservables.application.service.BuildingService;
import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(path = "/buildings", version = "1.0.0")
@RequiredArgsConstructor
@Tag(name = "buildings", description = "Endpoints for managing buildings")
@SecurityRequirement(name = "jwt")
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping("/{id}")
    @Operation(summary = "Get a building by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Building found", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "Building not found")
    })
    public BuildingDto getBuilding(@PathVariable long id) {
        return buildingService.getBuilding(id);
    }

    @GetMapping
    @Operation(summary = "Get all buildings")
    public List<BuildingDto> getAllBuildings(
            @RequestParam(defaultValue = "ACTIVE") ShowModeFilter showMode) {
        return buildingService.getAllBuildings(showMode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new building")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Building registered", useReturnTypeSchema = true, headers = {
                    @Header(name = "Location", description = "Relative URI to which retrieve the currently created building")}),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
            @ApiResponse(responseCode = "409", description = "Name already exists")
    })
    public ResponseEntity<@NonNull BuildingDto> registerBuilding(@RequestBody @Valid BuildingRegisterDto request) {
        BuildingDto response = buildingService.registerBuilding(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .pathSegment("{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing building")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Building updated", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
            @ApiResponse(responseCode = "404", description = "Building not found"),
            @ApiResponse(responseCode = "409", description = "Name already exists")
    })
    public BuildingDto updateBuilding(@PathVariable long id, @RequestBody @Valid BuildingUpdateDto request) {
        return buildingService.updateBuilding(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a building")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Building deactivated"),
            @ApiResponse(responseCode = "404", description = "Building not found")
    })
    public ResponseEntity<@NonNull Void> deactivateBuilding(@PathVariable long id) {
        buildingService.deactivateBuilding(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a building")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Building activated"),
            @ApiResponse(responseCode = "404", description = "Building not found")
    })
    public ResponseEntity<@NonNull Void> activateBuilding(@PathVariable long id) {
        buildingService.activateBuilding(id);
        return ResponseEntity.noContent().build();
    }
}
