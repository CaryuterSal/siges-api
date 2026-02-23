package dev.spiffocode.sigesapi.reservables.domain.exception;

public class BuildingNotFoundException extends ReservableNotFoundException {
    public BuildingNotFoundException(String message, long id) {
        super(message, id);
    }
}
