package dev.spiffocode.sigesapi.reservations.presentation;

import dev.spiffocode.sigesapi.users.presentation.dto.UserResponse;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Builder
@Jacksonized
public record NoteItem(
    Long id,
    String comment,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    UserResponse createdBy
) {
}
