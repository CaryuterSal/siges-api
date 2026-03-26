package dev.spiffocode.sigesapi.reservables.presentation.dto;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.time.Duration;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Jacksonized
@Value
public class SpaceSummaryDto extends ReservableDto{
    SpaceTypeDto spaceType;
    Duration bookInAdvanceDuration;
    Integer capacity;
}
