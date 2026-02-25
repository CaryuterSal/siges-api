package dev.spiffocode.sigesapi.reservables.presentation.controller;

import dev.spiffocode.sigesapi.common.presentation.ValidationProblem;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceService;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceUpdateDto;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(path = "/spaces", version = "1.0.0")
@RequiredArgsConstructor
@Tag(name = "spaces", description = "Endpoints for managing spaces")
@SecurityRequirement(name = "jwt")
public class SpaceController {

    private final SpaceService spaceService;

    @GetMapping("/{id}")
    @Operation(summary = "Get a space by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Space found", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "Space not found")
    })
    public SpaceDto getSpace(@PathVariable long id) {
        return spaceService.getSpaceById(id);
    }

    @GetMapping
    @Operation(summary = "Search spaces by filters")
    public List<SpaceDto> searchSpaces(
            @RequestParam(required = false) String searchQuery,
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) ReservableStatus statusFilter,
            @RequestParam(required = false) Long buildingIdFilter,
            @RequestParam(required = false) Boolean studentsAvailableFilter,
            @RequestParam(required = false) Long spaceTypeIdFilter,
            @RequestParam(name = "onlyActive", defaultValue = "true") Boolean onlyActiveFilter) {

        return spaceService.searchSpacesByFilter(searchQuery, pageable, statusFilter, buildingIdFilter,
                studentsAvailableFilter, spaceTypeIdFilter, onlyActiveFilter);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new space")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Space registered", useReturnTypeSchema = true, headers = {
                    @Header(name = "Location", description = "Relative URI to which retrieve the currently created equipment")}),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class)))
    })
    public ResponseEntity<@NonNull SpaceDto> registerSpace(@RequestBody @Valid SpaceRegisterDto request) {
        SpaceDto response = spaceService.registerSpace(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .pathSegment("{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing space")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Space updated", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
            @ApiResponse(responseCode = "404", description = "Space not found")
    })
    public SpaceDto updateSpace(@PathVariable long id, @RequestBody @Valid SpaceUpdateDto request) {
        return spaceService.updateSpace(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a space")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Space deactivated"),
            @ApiResponse(responseCode = "404", description = "Space not found")
    })
    public ResponseEntity<@NonNull Void> deactivateSpace(@PathVariable long id) {
        spaceService.deactivateSpace(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate a space")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Space activated"),
            @ApiResponse(responseCode = "404", description = "Space not found")
    })
    public ResponseEntity<@NonNull Void> activateSpace(@PathVariable long id) {
        spaceService.activateSpace(id);
        return ResponseEntity.noContent().build();
    }
}
