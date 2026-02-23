package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.presentation.BuildingDto;
import dev.spiffocode.sigesapi.reservables.presentation.BuildingRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.BuildingUpdateDto;

import java.util.List;

public interface BuildingService {
    BuildingDto getBuilding(long id);
    List<BuildingDto> getAllBuildings(boolean onlyActive);
    BuildingDto updateBuilding(long id, BuildingUpdateDto request);
    BuildingDto registerBuilding(BuildingRegisterDto  request);
    void deactivateBuilding(long id);
    void activateBuilding(long id);

}
