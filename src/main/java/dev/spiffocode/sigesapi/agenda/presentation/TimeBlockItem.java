package dev.spiffocode.sigesapi.agenda.presentation;

import dev.spiffocode.sigesapi.agenda.application.TimeRange;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalTime;

@Builder
@Jacksonized
public record TimeBlockItem(
        LocalTime start,
        LocalTime end
) implements TimeRange
{
}
