package dev.spiffocode.sigesapi.agenda.presentation;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.util.List;

@Builder
@Jacksonized
public record DayAvailabilityItem(
        LocalDate date,
        List<TimeBlockItem> availableBlocks,
        List<OccupiedBlockItem> occupiedBlocks
) {
}
