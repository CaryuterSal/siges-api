package dev.spiffocode.sigesapi.reservations.application.service;

import dev.spiffocode.sigesapi.reservations.presentation.ChangeReservationStatusRequest;
import dev.spiffocode.sigesapi.reservations.presentation.CreateReservationRequest;
import dev.spiffocode.sigesapi.reservations.presentation.RescheduleReservationRequest;
import dev.spiffocode.sigesapi.reservations.presentation.ReservationResponse;

public interface ReservationService {
    ReservationResponse createReservation(CreateReservationRequest request);
    ReservationResponse rescheduleReservation(Long id, RescheduleReservationRequest request);
    void changeReservationStatus(Long id, ChangeReservationStatusRequest request);
}
