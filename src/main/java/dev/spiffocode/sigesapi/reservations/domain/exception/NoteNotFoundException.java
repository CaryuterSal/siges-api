package dev.spiffocode.sigesapi.reservations.domain.exception;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.NotFoundException;

public class NoteNotFoundException extends NotFoundException {
    public NoteNotFoundException(Long noteId) {
        super("Note not found with id " + noteId, noteId.toString());
    }
}
