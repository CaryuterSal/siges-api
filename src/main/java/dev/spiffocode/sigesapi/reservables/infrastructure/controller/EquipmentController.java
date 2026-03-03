package dev.spiffocode.sigesapi.reservables.infrastructure.controller;

import dev.spiffocode.sigesapi.common.presentation.ValidationProblem;
import dev.spiffocode.sigesapi.reservables.application.service.EquipmentFilter;
import dev.spiffocode.sigesapi.reservables.application.service.EquipmentService;
import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentUpdateDto;
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
@RequestMapping(path = "/equipments", version = "1.0.0")
@RequiredArgsConstructor
@Tag(name = "equipments", description = "Endpoints for managing equipment")
@SecurityRequirement(name = "jwt")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping("/{id}")
    @Operation(summary = "Get an equipment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment found", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "Equipment not found")
    })
    public EquipmentDto getEquipment(@PathVariable long id) {
        return equipmentService.getEquipmentById(id);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Invalid sort field")
    })
    @GetMapping
    @PageableAsQueryParam
    @Operation(summary = "Search equipment by filters")
    public Page<@NonNull EquipmentDto> searchEquipments(
            @RequestParam(required = false)
            @Schema(description = "Query for searching an equipment by containing name, description or inventory ID Num.", example = "Teclado blanco")
            String searchQuery,
            @ParameterObject
            @SortDefault("updatedAt")
            @Schema(description = "Information por paging and sorting by any field")
            Pageable pageable,
            @RequestParam(required = false)
            @Schema(description = "Filter by simple equipment state (ACTIVE or MAINTENANCE)")
            ReservableStatus status,
            @RequestParam(required = false)
            @Schema(description = "Filter by building the equipment is located on")
            Long buildingId,
            @RequestParam(required = false)
            @Schema(description = "Filter depending on whether the equipment is visible to students or not")
            Boolean studentsAvailable,
            @RequestParam(required = false)
            @Schema(description = "Filter by space the equipment is contained on")
            Long spaceId,
            @RequestParam(required = false)
            @Schema(description = "Filter by the start time of a desired availability window", dependentRequiredMap = @DependentRequired("requestEnd"))
            LocalDateTime requestStart,
            @RequestParam(required = false)
            @Schema(description = "Filter by the end time of a desired availability window", dependentRequiredMap = @DependentRequired("requestEnd"))
            LocalDateTime requestEnd,
            @RequestParam(defaultValue = "ACTIVE")
            @Schema(description = "Whether to fetch only ACTIVE records, only DELETED records, or ALL")
            ShowModeFilter showMode) {

        EquipmentFilter filter = EquipmentFilter.builder()
                .statusFilter(status)
                .buildingIdFilter(buildingId)
                .studentsAvailableFilter(studentsAvailable)
                .spaceIdFilter(spaceId)
                .requestStartFilter(requestStart)
                .requestEndFilter(requestEnd)
                .showModeFilter(showMode).build();

        return equipmentService.searchEquipmentsByFilter(pageable, filter);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new equipment")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipment registered", useReturnTypeSchema = true, headers = {
                    @Header(name = "Location", description = "Relative URI to which retrieve the currently created equipment")}),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class)))
    })
    public ResponseEntity<@NonNull EquipmentDto> registerEquipment(@RequestBody @Valid EquipmentRegisterDto request) {

        EquipmentDto response = equipmentService.registerEquipment(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .pathSegment("{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing equipment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment updated", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
            @ApiResponse(responseCode = "404", description = "Equipment not found")
    })
    public EquipmentDto updateEquipment(@PathVariable long id, @RequestBody @Valid EquipmentUpdateDto request) {
        return equipmentService.updateEquipment(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate an equipment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Equipment deactivated"),
            @ApiResponse(responseCode = "404", description = "Equipment not found")
    })
    public ResponseEntity<@NonNull Void> deactivateEquipment(@PathVariable long id) {
        equipmentService.deactivateEquipment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate an equipment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Equipment activated"),
            @ApiResponse(responseCode = "404", description = "Equipment not found")
    })
    public ResponseEntity<@NonNull Void> activateEquipment(@PathVariable long id) {
        equipmentService.activateEquipment(id);
        return ResponseEntity.noContent().build();
    }
}
