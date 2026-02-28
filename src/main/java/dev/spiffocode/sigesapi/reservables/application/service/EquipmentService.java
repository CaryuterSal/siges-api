package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentUpdateDto;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EquipmentService {

    EquipmentDto getEquipmentById(long id);
    Page<@NonNull EquipmentDto> searchEquipmentsByFilter(Pageable pageable, EquipmentFilter equipmentFilter);
    EquipmentDto registerEquipment(EquipmentRegisterDto request);
    EquipmentDto updateEquipment(long id, EquipmentUpdateDto request);
    void deactivateEquipment(long id);
    void activateEquipment(long id);

}
