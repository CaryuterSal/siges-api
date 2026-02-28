package dev.spiffocode.sigesapi.reservables.domain.exception;

public class SpaceNotFoundException extends ReservableNotFoundException {

    public SpaceNotFoundException(String message, long id) {
        super(message, id);
    }
}
