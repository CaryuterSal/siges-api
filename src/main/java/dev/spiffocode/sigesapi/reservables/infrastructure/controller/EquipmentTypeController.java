package dev.spiffocode.sigesapi.reservables.infrastructure.controller;

import dev.spiffocode.sigesapi.common.presentation.ValidationProblem;
import dev.spiffocode.sigesapi.reservables.application.service.EquipmentTypeFilter;
import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.application.service.EquipmentTypeService;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeUpdateDto;
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
@RequestMapping(path = "/equipment-types", version = "1.0.0")
@RequiredArgsConstructor
@Tag(name = "Equipment Types", description = "Endpoints for managing equipment types")
@SecurityRequirement(name = "jwt")
public class EquipmentTypeController {

    private final EquipmentTypeService equipmentTypeService;

    @GetMapping("/{id}")
    @Operation(summary = "Get an equipment type by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "EquipmentType found", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "EquipmentType not found")
    })
    public EquipmentTypeDto getEquipmentType(@PathVariable long id) {
        return equipmentTypeService.getEquipmentType(id);
    }

    @GetMapping
    @Operation(summary = "Get all equipment types")
    public List<EquipmentTypeDto> getAllEquipmentTypes(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "ACTIVE") ShowModeFilter showMode) {
        EquipmentTypeFilter filter = EquipmentTypeFilter.builder()
                .showModeFilter(showMode)
                .query(q)
                .build();
        return equipmentTypeService.getAllEquipmentTypes(filter);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new equipment type")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "EquipmentType registered", useReturnTypeSchema = true, headers = {
                    @Header(name = "Location", description = "Relative URI to which retrieve the currently created equipment type") }),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class)))
    })
    public ResponseEntity<@NonNull EquipmentTypeDto> registerEquipmentType(
            @RequestBody @Valid EquipmentTypeRegisterDto request) {
        EquipmentTypeDto response = equipmentTypeService.registerEquipmentType(request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .pathSegment("{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing equipment type")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "EquipmentType updated", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
            @ApiResponse(responseCode = "404", description = "EquipmentType not found")
    })
    public EquipmentTypeDto updateEquipmentType(@PathVariable long id,
            @RequestBody @Valid EquipmentTypeUpdateDto request) {
        return equipmentTypeService.updateEquipmentType(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate an equipment type")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "EquipmentType deactivated"),
            @ApiResponse(responseCode = "404", description = "EquipmentType not found")
    })
    public ResponseEntity<@NonNull Void> deactivateEquipmentType(@PathVariable long id) {
        equipmentTypeService.deactivateEquipmentType(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate an equipment type")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "EquipmentType activated"),
            @ApiResponse(responseCode = "404", description = "EquipmentType not found")
    })
    public ResponseEntity<@NonNull Void> activateEquipmentType(@PathVariable long id) {
        equipmentTypeService.activateEquipmentType(id);
        return ResponseEntity.noContent().build();
    }
}
