package dev.spiffocode.sigesapi.reservations.infrastructure.web;

import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.SingleReservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.infrastructure.service.impl.ApproveRejectReservationUseCase;
import dev.spiffocode.sigesapi.reservations.infrastructure.service.impl.CreateReservationUseCase;
import dev.spiffocode.sigesapi.reservations.infrastructure.service.impl.GetReservationsQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

// presentation/controller/ReservationController.java
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations")
public class ReservationController {

    private final CreateReservationUseCase createReservationUseCase;
    private final ApproveRejectReservationUseCase approveRejectUseCase;

    @PostMapping
    @Operation(summary = "Crear reservación única o recurrente")
    public ResponseEntity<ReservationResponseDTO> create(
        @RequestBody @Valid CreateReservationRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long applicantId = extractId(userDetails);
        Reservation saved = createReservationUseCase.execute(
            request.toCommand(), applicantId);
        return ResponseEntity
            .created(URI.create("/reservations/" + saved.getId()))
            .body(mapper.toDTO(saved));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprobar o rechazar una reservación")
    public ResponseEntity<Void> updateStatus(
        @PathVariable Long id,
        @RequestBody @Valid UpdateReservationStatusRequest request
    ) {
        approveRejectUseCase.execute(new UpdateReservationStatusCommand(
            id, request.action(), request.comment()
        ));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar una reservación o una ocurrencia de una serie")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestBody @Valid CancelReservationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long requesterId = extractId(userDetails);
        cancelUseCase.execute(new CancelReservationCommand(
                id,
                request.scope(),
                request.occurrenceDate(),
                request.reason()
        ), requesterId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/finish")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Finalizar una reservación manualmente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservación finalizada"),
            @ApiResponse(responseCode = "404", description = "Reservación no encontrada"),
            @ApiResponse(responseCode = "409", description = "La reservación no está en estado APPROVED")
    })
    public ResponseEntity<Void> finish(@PathVariable Long id) {
        finishReservationUseCase.execute(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/occurrences/{date}/modify")
    @Operation(summary = "Solicitar modificación de una ocurrencia específica")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Modificación solicitada, pendiente de aprobación"),
            @ApiResponse(responseCode = "404", description = "Reservación no encontrada"),
            @ApiResponse(responseCode = "409", description = "La ocurrencia ya fue modificada o cancelada"),
            @ApiResponse(responseCode = "422", description = "Fecha fuera de rango o sin anticipación suficiente")
    })
    public ResponseEntity<ReservationResponseDTO> modifyOccurrence(
            @PathVariable Long id,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody @Valid ModifyOccurrenceRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        SingleReservation replacement = modifyOccurrenceUseCase.execute(
                new ModifyOccurrenceCommand(
                        id,
                        date,
                        request.newDate(),
                        request.newStartTime(),
                        request.newEndTime(),
                        request.reason()
                ),
                extractId(userDetails)
        );

        return ResponseEntity
                .created(URI.create("/reservations/" + replacement.getId()))
                .body(mapper.toDTO(replacement));
    }


    @GetMapping
    public ResponseEntity<Page<ReservationSummaryDTO>> getAll(
            @RequestParam(required = false) List<Status> statuses,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) Long reservableId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long applicantId = isAdmin(userDetails) ? null : extractId(userDetails);

        GetReservationsQuery query = new GetReservationsQuery(
                applicantId, statuses, dateFrom, dateTo, reservableId, page, size, "createdAt", "DESC"
        );

        return ResponseEntity.ok(getReservationsUseCase.execute(query));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<ReservationHistoryDTO>> getHistory(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long applicantId = isAdmin(userDetails) ? null : extractId(userDetails);

        GetReservationsQuery query = new GetReservationsQuery(
                applicantId,
                List.of(Status.FINISHED, Status.CANCELLED, Status.REJECTED),
                dateFrom, dateTo, null, page, size, "createdAt", "DESC"
        );

        return ResponseEntity.ok(getReservationsUseCase.getHistory(query));
    }

}