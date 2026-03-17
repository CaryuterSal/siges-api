package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeUpdateDto;

import java.util.List;

public interface EquipmentTypeService {
    EquipmentTypeDto getEquipmentType(long id);

    List<EquipmentTypeDto> getAllEquipmentTypes(EquipmentTypeFilter filter);

    EquipmentTypeDto updateEquipmentType(long id, EquipmentTypeUpdateDto request);

    EquipmentTypeDto registerEquipmentType(EquipmentTypeRegisterDto request);

    void deactivateEquipmentType(long id);

    void activateEquipmentType(long id);
}
