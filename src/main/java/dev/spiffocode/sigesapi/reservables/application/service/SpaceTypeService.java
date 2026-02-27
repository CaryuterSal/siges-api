package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeUpdateDto;

import java.util.List;

public interface SpaceTypeService {
    SpaceTypeDto getSpaceType(long id);

    List<SpaceTypeDto> getAllSpaceTypes(ShowModeFilter showModeFilter);

    SpaceTypeDto updateSpaceType(long id, SpaceTypeUpdateDto request);

    SpaceTypeDto registerSpaceType(SpaceTypeRegisterDto request);

    void deactivateSpaceType(long id);

    void activateSpaceType(long id);
}
