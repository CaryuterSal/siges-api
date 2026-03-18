package dev.spiffocode.sigesapi.reservables.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.NotFoundException;

public class SpaceAssetNotFoundException extends NotFoundException {
    public SpaceAssetNotFoundException(String message, long id) {
        super(message, String.valueOf(id));
    }

    public SpaceAssetNotFoundException(long id) {
        super("No se encontró el equipo de especificación técnica con el id %dl".formatted(id), String.valueOf(id));
    }
}
