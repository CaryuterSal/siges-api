package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.presentation.BuildingDto;
import dev.spiffocode.sigesapi.reservables.presentation.SpaceTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.SpaceTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.SpaceTypeUpdateDto;

import java.util.List;

public interface SpaceTypeService {
    BuildingDto getSpaceType(long id);
    List<SpaceTypeDto> getAllSpaceTypes(boolean onlyActive);
    SpaceTypeDto updateSpaceType(long id, SpaceTypeUpdateDto request);
    SpaceTypeDto registerSpaceType(SpaceTypeRegisterDto request);
    void deactivateSpaceType(long id);
    void activateSpaceType(long id);
}
