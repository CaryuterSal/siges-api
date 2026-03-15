package dev.spiffocode.sigesapi.reservations.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.NotFoundException;

public class ReservationNotFoundException extends NotFoundException {
    public ReservationNotFoundException(Long id){
        super("Reservation not found with id " + id, id.toString());
    }
}
