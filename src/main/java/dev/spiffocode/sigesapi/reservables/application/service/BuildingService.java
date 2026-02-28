package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingUpdateDto;

import java.util.List;

public interface BuildingService {
    BuildingDto getBuilding(long id);
    List<BuildingDto> getAllBuildings(ShowModeFilter showModeFilter);
    BuildingDto updateBuilding(long id, BuildingUpdateDto request);
    BuildingDto registerBuilding(BuildingRegisterDto  request);
    void deactivateBuilding(long id);
    void activateBuilding(long id);

}
