package dev.spiffocode.sigesapi.reservables.presentation.dto;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.time.Duration;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Value
public class SpaceDto extends  ReservableDto {
    SpaceTypeDto spaceType;
    Duration bookInAdvanceDuration;
}
