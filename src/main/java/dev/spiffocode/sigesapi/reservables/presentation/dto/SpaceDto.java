package dev.spiffocode.sigesapi.reservables.presentation.dto;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.time.Duration;


@Setter
@SuperBuilder
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@Value
public class SpaceDto extends  ReservableDto {
    SpaceTypeDto spaceType;
    Duration bookInAdvanceDuration;
}
