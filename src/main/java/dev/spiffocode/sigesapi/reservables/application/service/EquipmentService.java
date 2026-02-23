package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentUpdateDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EquipmentService {

    EquipmentDto getEquipmentById(long id);
    List<EquipmentDto> searchEquipmentsByFilter(String searchQuery, Pageable pageable, ReservableStatus statusFilter, Long buildingIdFilter, Boolean studentsAvailableFilter, Long spaceIdFilter, Boolean onlyActiveFilter);
    EquipmentDto registerEquipment(EquipmentRegisterDto request);
    EquipmentDto updateEquipment(long id, EquipmentUpdateDto request);
    void deactivateEquipment(long id);
    void activateEquipment(long id);

}
