package dev.spiffocode.sigesapi.reservables.infrastructure.controller;

import dev.spiffocode.sigesapi.common.presentation.ValidationProblem;
import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceAssetFilter;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceAssetService;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
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

@RestController
@RequestMapping(path = "/spaces", version = "1.0.0")
@RequiredArgsConstructor
@Tags({
        @Tag(name = "Space", description = "Endpoints for managing spaces"),
        @Tag(name = "Space Assets", description = "Endpoints for managing spaces technical assets (included non-reservable equipments)")
})
@SecurityRequirement(name = "jwt")
public class SpaceAssetController {

    private final SpaceAssetService spaceAssetService;

    @GetMapping("/assets/{id}")
    @Operation(summary = "Get an space asset by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Space asset found", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "Space asset not found")
    })
    public SpaceAssetDto getSpaceAsset(@PathVariable long id) {
        return spaceAssetService.getSpaceAssetById(id);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Invalid sort field")
    })
    @GetMapping("/assets")
    @PageableAsQueryParam
    @Operation(summary = "Search asset by filters")
    public Page<@NonNull SpaceAssetDto> searchSpaceAssets(
            @RequestParam(required = false)
            @Schema(description = "Query for searching an asset by containing name, description or inventory ID Num.", example = "Teclado blanco")
            String searchQuery,
            @ParameterObject
            @SortDefault("updatedAt")
            @Schema(description = "Information por paging and sorting by any field")
            Pageable pageable,
            @RequestParam(required = false)
            @Schema(description = "Filter by building the asset is located on")
            Long buildingId,
            @RequestParam(required = false)
            @Schema(description = "Filter by space the asset is contained on")
            Long spaceId,
            @RequestParam(required = false)
            @Schema(description = "Filter by the type of the asset (as equipment)")
            Long equipmentTypeId,
            @RequestParam(defaultValue = "ACTIVE")
            @Schema(description = "Whether to fetch only ACTIVE records, only DELETED records, or ALL")
            ShowModeFilter showMode) {

        SpaceAssetFilter filter = SpaceAssetFilter.builder()
                .searchQuery(searchQuery)
                .buildingIdFilter(buildingId)
                .equipmentTypeIdFilter(equipmentTypeId)
                .spaceIdFilter(spaceId)
                .showModeFilter(showMode).build();

        return spaceAssetService.searchSpaceAssetsByFilter(pageable, filter);
    }

    @PostMapping("/{spaceId}/assets")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new asset within an space scope")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Space asset registered", useReturnTypeSchema = true, headers = {
                    @Header(name = "Location", description = "Relative URI to which retrieve the currently created asset")}),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
            @ApiResponse(responseCode = "409", description = "Name or inventory num is not unique")
    })
    public ResponseEntity<@NonNull SpaceAssetDto> registerSpaceAsset(@PathVariable Long spaceId, @RequestBody @Valid SpaceAssetRegisterDto request) {

        SpaceAssetDto response = spaceAssetService.registerSpaceAsset(spaceId, request);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .pathSegment("/spaces/assets/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/assets/{id}")
    @Operation(summary = "Update an existing equipment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Space asset updated", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
            @ApiResponse(responseCode = "404", description = "Space asset not found"),
            @ApiResponse(responseCode = "409", description = "Name or inventory num is not unique")
    })
    public SpaceAssetDto updateSpaceAsset(@PathVariable long id, @RequestBody @Valid SpaceAssetUpdateDto request) {
        return spaceAssetService.updateSpaceAsset(id, request);
    }

    @PatchMapping("/assets/{id}/deactivate")
    @Operation(summary = "Deactivate an equipment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "SpaceAsset deactivated"),
            @ApiResponse(responseCode = "404", description = "SpaceAsset not found")
    })
    public ResponseEntity<@NonNull Void> deactivateSpaceAsset(@PathVariable long id) {
        spaceAssetService.deactivateSpaceAsset(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/assets/{id}/activate")
    @Operation(summary = "Activate an equipment")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "SpaceAsset activated"),
            @ApiResponse(responseCode = "404", description = "SpaceAsset not found")
    })
    public ResponseEntity<@NonNull Void> activateSpaceAsset(@PathVariable long id) {
        spaceAssetService.activateSpaceAsset(id);
        return ResponseEntity.noContent().build();
    }
}
