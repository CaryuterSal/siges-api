package dev.spiffocode.sigesapi.reservations.domain.exception;

public class ReservableNotAvailableForStudentsException extends RuntimeException {
    public ReservableNotAvailableForStudentsException(Long id){
        super("Reservable not available for student " + id);
    }
}
