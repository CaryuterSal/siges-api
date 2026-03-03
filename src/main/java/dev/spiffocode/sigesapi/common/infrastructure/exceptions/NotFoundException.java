package dev.spiffocode.sigesapi.common.infrastructure.exceptions;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

    private final String id;

    public NotFoundException(String message, String id) {
        super(message);
        this.id = id;
    }
}
