package dev.spiffocode.sigesapi.reservables.presentation.dto;

import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.time.Duration;

@SuperBuilder
@Jacksonized
public class SpaceSummaryDto extends ReservableDto{
    SpaceTypeDto spaceType;
    Duration bookInAdvanceDuration;
    Integer capacity;
}
