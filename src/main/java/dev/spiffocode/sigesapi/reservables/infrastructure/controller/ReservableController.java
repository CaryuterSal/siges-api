package dev.spiffocode.sigesapi.reservables.infrastructure.controller;

import dev.spiffocode.sigesapi.reservables.application.service.ReservableFilter;
import dev.spiffocode.sigesapi.reservables.application.service.ReservableService;
import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.presentation.dto.ReservableDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.DependentRequired;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping(path = "/reservables", version = "1.0.0")
@RequiredArgsConstructor
@Tag(name = "reservables", description = "Endpoints for searching across all reservables (Spaces and Equipments)")
@SecurityRequirement(name = "jwt")
public class ReservableController {

    private final ReservableService reservableService;

    @GetMapping("/{id}")
    @Operation(summary = "Get a reservable by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservable found", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "Reservable not found")
    })
    public ReservableDto getReservable(@PathVariable long id) {
        return reservableService.getReservableById(id);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "Invalid sort field")
    })
    @GetMapping
    @PageableAsQueryParam
    @Operation(summary = "Search reservables by filters")
    public Page<@NonNull ReservableDto> searchReservables(
            @RequestParam(required = false) @Schema(description = "Query for searching a reservable by containing name or description String", example = "auditorio") String searchQuery,
            @ParameterObject @SortDefault("updatedAt") @Schema(description = "Information for paging and sorting by any field") Pageable pageable,
            @RequestParam(required = false) @Schema(description = "Filter by simple reservable state (AVAILABLE, MAINTENANCE, etc.)") ReservableStatus status,
            @RequestParam(required = false) @Schema(description = "Filter by building where the reservable is located") Long buildingId,
            @RequestParam(required = false) @Schema(description = "Filter depending on whether the reservable is available to students") Boolean studentsAvailable,
            @RequestParam(required = false) @Schema(description = "Filter by the start time of a desired availability window", dependentRequiredMap = @DependentRequired("requestEnd")) LocalDateTime requestStart,
            @RequestParam(required = false) @Schema(description = "Filter by the end time of a desired availability window", dependentRequiredMap = @DependentRequired("requestEnd")) LocalDateTime requestEnd,
            @RequestParam(defaultValue = "ACTIVE") @Schema(description = "Whether to fetch only ACTIVE records, only INACTIVE records, or ALL") ShowModeFilter showMode) {

        ReservableFilter filter = ReservableFilter.builder()
                .searchQuery(searchQuery)
                .buildingIdFilter(buildingId)
                .studentsAvailableFilter(studentsAvailable)
                .requestStartFilter(requestStart)
                .requestEndFilter(requestEnd)
                .showModeFilter(showMode)
                .status(status)
                .build();

        return reservableService.searchReservablesByFilter(pageable, filter);
    }
}
