package dev.spiffocode.sigesapi.reservables.presentation.controller;

import dev.spiffocode.sigesapi.common.presentation.ValidationProblem;
import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceTypeService;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeUpdateDto;
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
@RequestMapping(path = "/spacetypes", version = "1.0.0")
@RequiredArgsConstructor
@Tag(name = "spacetypes", description = "Endpoints for managing space types")
@SecurityRequirement(name = "jwt")
public class SpaceTypeController {

    private final SpaceTypeService spaceTypeService;

    @GetMapping("/{id}")
    @Operation(summary = "Get a space type by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SpaceType found", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "SpaceType not found")
    })
    public SpaceTypeDto getSpaceType(@PathVariable long id) {
        return spaceTypeService.getSpaceType(id);
    }

    @GetMapping
    @Operation(summary = "Get all space types")
    public List<SpaceTypeDto> getAllSpaceTypes(
            @RequestParam(defaultValue = "ACTIVE") ShowModeFilter showMode) {
        return spaceTypeService.getAllSpaceTypes(showMode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new space type")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "SpaceType registered", useReturnTypeSchema = true, headers = {
                    @Header(name = "Location", description = "Relative URI to which retrieve the currently created equipment")}),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class)))
    })
    public ResponseEntity<@NonNull SpaceTypeDto> registerSpaceType(@RequestBody @Valid SpaceTypeRegisterDto request) {
        SpaceTypeDto response = spaceTypeService.registerSpaceType(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .pathSegment("{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing space type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "SpaceType updated", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
            @ApiResponse(responseCode = "404", description = "SpaceType not found")
    })
    public SpaceTypeDto updateSpaceType(@PathVariable long id, @RequestBody @Valid SpaceTypeUpdateDto request) {
        return spaceTypeService.updateSpaceType(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate a space type")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "SpaceType deactivated"),
            @ApiResponse(responseCode = "404", description = "SpaceType not found")
    })
    public ResponseEntity<@NonNull Void> deactivateSpaceType(@PathVariable long id) {
        spaceTypeService.deactivateSpaceType(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate a space type")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "SpaceType activated"),
            @ApiResponse(responseCode = "404", description = "SpaceType not found")
    })
    public ResponseEntity<@NonNull Void> activateSpaceType(@PathVariable long id) {
        spaceTypeService.activateSpaceType(id);
        return ResponseEntity.noContent().build();
    }
}
