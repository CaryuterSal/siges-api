package dev.spiffocode.sigesapi.reservables.presentation.controller;

import dev.spiffocode.sigesapi.common.presentation.ValidationProblem;
import dev.spiffocode.sigesapi.reservables.application.service.EquipmentService;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/equipments", version = "1.0.0")
@RequiredArgsConstructor
@Tag(name = "equipments", description = "Endpoints for managing equipment")
@SecurityRequirement(name = "jwt")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get an equipment by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipment found", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "Equipment not found")
    })
    public EquipmentDto getEquipment(@PathVariable long id) {
        return equipmentService.getEquipmentById(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Search equipment by filters")
    public List<EquipmentDto> searchEquipments(
            @RequestParam(required = false) String searchQuery,
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) ReservableStatus statusFilter,
            @RequestParam(required = false) Long buildingIdFilter,
            @RequestParam(required = false) Boolean studentsAvailableFilter,
            @RequestParam(required = false) Long spaceIdFilter,
            @RequestParam(name = "onlyActive", defaultValue = "true") Boolean onlyActiveFilter) {

        return equipmentService.searchEquipmentsByFilter(searchQuery, pageable, statusFilter, buildingIdFilter,
                studentsAvailableFilter, spaceIdFilter, onlyActiveFilter);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new equipment")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Equipment registered", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class)))
    })
    public EquipmentDto registerEquipment(@RequestBody @Valid EquipmentRegisterDto request) {
        return equipmentService.registerEquipment(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate an equipment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Equipment deactivated"),
            @ApiResponse(responseCode = "404", description = "Equipment not found")
    })
    public ResponseEntity<Void> deactivateEquipment(@PathVariable long id) {
        equipmentService.deactivateEquipment(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate an equipment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Equipment activated"),
            @ApiResponse(responseCode = "404", description = "Equipment not found")
    })
    public ResponseEntity<Void> activateEquipment(@PathVariable long id) {
        equipmentService.activateEquipment(id);
        return ResponseEntity.noContent().build();
    }
}
