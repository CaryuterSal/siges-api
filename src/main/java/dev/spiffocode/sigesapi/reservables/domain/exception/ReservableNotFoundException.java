package dev.spiffocode.sigesapi.reservables.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.NotFoundException;

public class ReservableNotFoundException extends NotFoundException {
    public ReservableNotFoundException(String message, long id) {
        super(message, String.valueOf(id));
    }

    public ReservableNotFoundException(long id) {
        super("No se encontró el espacio o equipo con el id %dl".formatted(id), String.valueOf(id));
    }
}
