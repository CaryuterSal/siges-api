package dev.spiffocode.sigesapi.reservations.infrastructure.web;

import dev.spiffocode.sigesapi.common.presentation.ValidationProblem;
import dev.spiffocode.sigesapi.reservations.application.service.ReservationService;
import dev.spiffocode.sigesapi.reservations.domain.model.GroupingType;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.infrastructure.service.impl.ReservationFilterRequest;
import dev.spiffocode.sigesapi.reservations.presentation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(path = "/reservations")
@RequiredArgsConstructor
@Tag(name = "reservations", description = "Endpoints for managing reservations")
public class ReservationController {

        private final ReservationService reservationService;

        @GetMapping("/{id}")
        @Operation(summary = "Get a reservation by ID")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Reservation found", useReturnTypeSchema = true),
                        @ApiResponse(responseCode = "404", description = "Reservation not found")
        })
        public ReservationResponse getReservation(@PathVariable Long id) {
                return reservationService.getReservation(id);
        }

        @GetMapping
        @PageableAsQueryParam
        @Operation(summary = "Search reservations by filters")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "OK", useReturnTypeSchema = true),
                        @ApiResponse(responseCode = "400", description = "Invalid filter or sort field")
        })
        public Page<@NonNull ReservationResponse> getReservations(
                        @RequestParam(required = false) @Schema(description = "Filter by petitioner user ID") Long petitionerId,

                        @RequestParam(required = false) @Schema(description = "Filter by petitioner name (partial, case-insensitive)") String petitionerName,

                        @RequestParam(required = false) @Schema(description = "Filter by exact reservation date") LocalDate date,

                        @RequestParam(required = false) @Schema(description = "Filter reservations from this date (inclusive). Ignored if 'date' is set.") LocalDate dateFrom,

                        @RequestParam(required = false) @Schema(description = "Filter reservations until this date (inclusive). Ignored if 'date' is set.") LocalDate dateTo,

                        @RequestParam(required = false) @Schema(description = "Filter by reservation statuses") List<Status> statuses,

                        @RequestParam(required = false) @Schema(description = "Filter by reservable ID") Long reservableId,

                        @RequestParam(required = false) @Schema(description = "Filter by grouping type (SINGLE or GROUP)") GroupingType type,

                        @RequestParam(required = false) @Schema(description = "Search query (reservable name, building name, or petitioner name)") String q,

                        @ParameterObject @SortDefault("date") Pageable pageable) {

                ReservationFilterRequest filter = ReservationFilterRequest.builder()
                                .petitionerId(petitionerId)
                                .petitionerName(petitionerName)
                                .date(date)
                                .dateFrom(dateFrom)
                                .dateTo(dateTo)
                                .statuses(statuses)
                                .reservableId(reservableId)
                                .type(type)
                                .q(q)
                                .build();

                return reservationService.getReservations(filter, pageable);
        }

        @PostMapping
        @Operation(summary = "Create a new reservation")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Reservation created", useReturnTypeSchema = true, headers = @Header(name = "Location", description = "URI to retrieve the created reservation")),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
                        @ApiResponse(responseCode = "404", description = "Reservable or user not found"),
                        @ApiResponse(responseCode = "409", description = "Reservation overlaps with an existing one"),
                        @ApiResponse(responseCode = "422", description = "Reservation of space was made with too little anticipation")
        })
        public ResponseEntity<@NonNull ReservationResponse> createReservation(
                        @RequestBody @Valid CreateReservationRequest request) {
                ReservationResponse response = reservationService.createReservation(request);
                URI location = ServletUriComponentsBuilder
                                .fromCurrentRequestUri()
                                .pathSegment("{id}")
                                .buildAndExpand(response.id())
                                .toUri();
                return ResponseEntity.created(location).body(response);
        }

        @PatchMapping("/{id}")
        @Operation(summary = "Reschedule a reservation", description = "Only the petitioner can reschedule. Moves the reservation back to PENDING status.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Reservation rescheduled", useReturnTypeSchema = true),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
                        @ApiResponse(responseCode = "403", description = "Only the petitioner can reschedule"),
                        @ApiResponse(responseCode = "404", description = "Reservation not found"),
                        @ApiResponse(responseCode = "409", description = "New time slot overlaps with an existing reservation"),
                        @ApiResponse(responseCode = "422", description = "Reservation of space was made with too little anticipation")
        })
        public ReservationResponse rescheduleReservation(
                        @PathVariable Long id,
                        @RequestBody @Valid RescheduleReservationRequest request) {
                return reservationService.rescheduleReservation(id, request);
        }

        @PatchMapping("/{id}/approve")
        @Operation(summary = "Approve a reservation", description = "Admin only. Reservation must be in PENDING status.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Reservation approved", useReturnTypeSchema = true),
                        @ApiResponse(responseCode = "404", description = "Reservation not found"),
                        @ApiResponse(responseCode = "409", description = "Reservation is not in PENDING status")
        })
        public ReservationResponse approveReservation(
                        @PathVariable Long id,
                        @RequestBody @Valid ApproveReservationRequest request) {
                return reservationService.approveReservation(id, request);
        }

        @PatchMapping("/{id}/reject")
        @Operation(summary = "Reject a reservation", description = "Admin only. Reservation must be in PENDING status.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Reservation rejected", useReturnTypeSchema = true),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
                        @ApiResponse(responseCode = "404", description = "Reservation not found"),
                        @ApiResponse(responseCode = "409", description = "Reservation is not in PENDING status")
        })
        public ReservationResponse rejectReservation(
                        @PathVariable Long id,
                        @RequestBody @Valid RejectReservationRequest request) {
                return reservationService.rejectReservation(id, request);
        }

        @PatchMapping("/{id}/cancel")
        @Operation(summary = "Cancel a reservation", description = "Petitioner can cancel their own reservation. Admins can cancel any.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Reservation cancelled", useReturnTypeSchema = true),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
                        @ApiResponse(responseCode = "403", description = "Only the petitioner or an admin can cancel"),
                        @ApiResponse(responseCode = "404", description = "Reservation not found"),
                        @ApiResponse(responseCode = "409", description = "Reservation cannot be cancelled in its current status")
        })
        public ReservationResponse cancelReservation(
                        @PathVariable Long id,
                        @RequestBody @Valid CancelReservationRequest request) {
                return reservationService.cancelReservation(id, request);
        }

        @PatchMapping("/{id}/start")
        @Operation(summary = "Mark a reservation as Started", description = "Admin only. Reservation must be in APPROVED status.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Reservation finished", useReturnTypeSchema = true),
                        @ApiResponse(responseCode = "404", description = "Reservation not found"),
                        @ApiResponse(responseCode = "409", description = "Reservation is not in APPROVED status")
        })
        public ReservationResponse startReservation(@PathVariable Long id) {
                return reservationService.startReservation(id);
        }

    @PatchMapping("/{id}/finish")
        public ReservationResponse finishReservation(
                        @PathVariable Long id,
                        @RequestBody(required = false) @Valid FinishReservationRequest request) {
                return reservationService.finishReservation(id, request);
        }

        @PostMapping("/{id}/notes")
        @ResponseStatus(HttpStatus.CREATED)
        @Operation(summary = "Add a note to a reservation")
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Note added", useReturnTypeSchema = true),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
                        @ApiResponse(responseCode = "404", description = "Reservation not found")
        })
        public ReservationResponse addNote(
                        @PathVariable Long id,
                        @RequestBody @Valid PublishNoteRequest request) {
                return reservationService.addNote(id, request);
        }

        @PatchMapping("/{id}/notes/{noteId}")
        @Operation(summary = "Edit a note on a reservation", description = "Users can only edit their own notes. Admins can edit any note.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Note updated", useReturnTypeSchema = true),
                        @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ValidationProblem.class))),
                        @ApiResponse(responseCode = "403", description = "You can only edit your own notes"),
                        @ApiResponse(responseCode = "404", description = "Reservation or note not found")
        })
        public NoteItem editNote(
                        @PathVariable Long id,
                        @PathVariable Long noteId,
                        @RequestBody @Valid EditNoteRequest request) {
                return reservationService.editNote(id, noteId, request);
        }
}