package dev.spiffocode.sigesapi.reservables.infrastructure.controller;

import dev.spiffocode.sigesapi.common.presentation.ValidationProblem;
import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceFilter;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceService;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.DependentRequired;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;

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

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Invalid sort field")
    })
    @GetMapping
    @PageableAsQueryParam
    @Operation(summary = "Search spaces by filters")
    public Page<@NonNull SpaceDto> searchSpaces(
            @RequestParam(required = false)
            @Schema(description = "Query for searching an space by containing name or description String", example = "auditorio")
            String searchQuery,
            @ParameterObject
            @SortDefault("updatedAt")
            @Schema(description = "Information por paging and sorting by any field")
            Pageable pageable,
            @RequestParam(required = false)
            @Schema(description = "Filter by simple space state (ACTIVE or MAINTENANCE)")
            ReservableStatus status,
            @RequestParam(required = false)
            @Schema(description = "Filter by building the space is which the space is located")
            Long buildingId,
            @RequestParam(required = false)
            @Schema(description = "Filter depending on whether the space is visible to students or not")
            Boolean studentsAvailable,
            @RequestParam(required = false)
            @Schema(description = "Filter by the Space Type that categorizes this space")
            Long spaceTypeIdFilter,
            @RequestParam(required = false)
            @Schema(description = "Filter by the start time of a desired availability window", dependentRequiredMap = @DependentRequired("requestEnd"))
            LocalDateTime requestStart,
            @RequestParam(required = false)
            @Schema(description = "Filter by the end time of a desired availability window", dependentRequiredMap = @DependentRequired("requestEnd"))
            LocalDateTime requestEnd,
            @RequestParam(required = false)
            @Schema(description = "Filter by the minimum number of people the space should accommodate.")
            Integer capacity,
            @RequestParam(defaultValue = "ACTIVE")
            @Schema(description = "Whether to fetch only ACTIVE records, only DELETED records, or ALL")
            ShowModeFilter showMode) {

        SpaceFilter filter = SpaceFilter.builder()
                .searchQuery(searchQuery)
                .buildingIdFilter(buildingId)
                .studentsAvailableFilter(studentsAvailable)
                .spaceTypeIdFilter(spaceTypeIdFilter)
                .requestStartFilter(requestStart)
                .requestEndFilter(requestEnd)
                .showModeFilter(showMode)
                .capacityAtLeastFilter(capacity)
                .build();

        return spaceService.searchSpacesByFilter(pageable, filter);
    }

    @PostMapping
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
