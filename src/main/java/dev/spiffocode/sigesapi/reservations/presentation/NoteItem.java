package dev.spiffocode.sigesapi.reservations.presentation;

import dev.spiffocode.sigesapi.users.presentation.dto.AdminResponse;

import java.time.LocalDateTime;

public record NoteItem(
        Long id,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        AdminResponse createdBy

) {
}
