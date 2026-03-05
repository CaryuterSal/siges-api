package dev.spiffocode.sigesapi.agenda.presentation;

import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalTime;

@Builder
@Jacksonized
public record OccupiedBlockItem(
        LocalTime start,
        LocalTime end,
        Status status
) {
}
