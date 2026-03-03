package dev.spiffocode.sigesapi.reservables.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.NotFoundException;

public class ReservableNotFoundException extends NotFoundException {
    public ReservableNotFoundException(String message, long id) {
        super(message, String.valueOf(id));
    }
}
