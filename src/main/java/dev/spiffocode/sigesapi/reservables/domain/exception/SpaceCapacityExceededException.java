package dev.spiffocode.sigesapi.reservables.domain.exception;

public class SpaceCapacityExceededException extends RuntimeException {
    public SpaceCapacityExceededException(String message) {
        super(message);
    }
    public SpaceCapacityExceededException(int maxCapacity, int capacityRequested){
        super("El espacio no puede soportar dicha capacidad. Máximo: %d ; Solicitada: %d".formatted(maxCapacity, capacityRequested));
    }
}
