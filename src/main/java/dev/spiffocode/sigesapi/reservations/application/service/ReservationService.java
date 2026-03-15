package dev.spiffocode.sigesapi.reservations.application.service;

import dev.spiffocode.sigesapi.reservations.infrastructure.service.impl.ReservationFilterRequest;
import dev.spiffocode.sigesapi.reservations.presentation.*;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservationService {
    ReservationResponse createReservation(CreateReservationRequest request);
    ReservationResponse rescheduleReservation(Long id, RescheduleReservationRequest request);
    ReservationResponse approveReservation(Long id);
    ReservationResponse rejectReservation(Long id, RejectReservationRequest request);
    ReservationResponse cancelReservation(Long id, CancelReservationRequest request);
    ReservationResponse startReservation(Long id);
    ReservationResponse finishReservation(Long id);
    ReservationResponse addNote(Long reservationId, PublishNoteRequest request);
    NoteItem editNote(Long reservationId, Long noteId, EditNoteRequest request);
    ReservationResponse getReservation(Long id);
    Page<@NonNull ReservationResponse> getReservations(ReservationFilterRequest filter, Pageable pageable);
}
