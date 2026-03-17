package dev.spiffocode.sigesapi.reservables.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;

public class SpaceAssetExistsException extends ConflictingStateException {
    public SpaceAssetExistsException(String message) {
        super(message);
    }
}
