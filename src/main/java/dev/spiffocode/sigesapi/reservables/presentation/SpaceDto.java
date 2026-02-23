package dev.spiffocode.sigesapi.reservables.presentation;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.time.Duration;

@EqualsAndHashCode(callSuper = true)
@Value
public class SpaceDto extends  ReservableDto {
    SpaceTypeDto spaceType;
    Duration bookInAdvanceDuration;
}
