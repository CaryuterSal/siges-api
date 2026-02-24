package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.reservables.application.mapper.EquipmentMapper;
import dev.spiffocode.sigesapi.reservables.application.service.EquipmentService;
import dev.spiffocode.sigesapi.reservables.domain.exception.BuildingNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.exception.ReservableNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.exception.SpaceNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentUpdateDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dev.spiffocode.sigesapi.common.domain.specification.SpecificationHelper.cast;
import static dev.spiffocode.sigesapi.reservables.domain.specification.EquipmentSpecifications.inSpace;
import static dev.spiffocode.sigesapi.reservables.domain.specification.EquipmentSpecifications.inventoryNumContains;
import static dev.spiffocode.sigesapi.reservables.domain.specification.ReservableSpecifications.*;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentMapper equipmentMapper;
    private final SpaceRepository spaceRepository;
    private final BuildingRepository buildingRepository;

    @Override
    public EquipmentDto getEquipmentById(long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ReservableNotFoundException("Equipment with ID %dl not found".formatted(id), id));
        return equipmentMapper.toDto(equipment);
    }

    @Override
    public Page<@NonNull EquipmentDto> searchEquipmentsByFilter(String searchQuery,
                                                                Pageable pageable,
                                                                ReservableStatus statusFilter,
                                                                Long buildingIdFilter,
                                                                Boolean studentsAvailableFilter,
                                                                Long spaceIdFilter,
                                                                Boolean onlyActiveFilter)
    {
            Specification<@NonNull Equipment> spec = Specification
                .where(inSpace(spaceIdFilter))
                .and(cast(statusIs(statusFilter)))
                .and(
                    inventoryNumContains(searchQuery)
                    .or(cast(descriptionContains(searchQuery)))
                    .or(cast(nameContains(searchQuery)))
                )
                .and(cast(inBuilding(buildingIdFilter)))
                .and(cast(availableForStudents(studentsAvailableFilter)));

        return equipmentRepository.findAll(spec, pageable)
                .map(equipmentMapper::toDto);
    }

    @Override
    public EquipmentDto registerEquipment(EquipmentRegisterDto request) {
        Long spaceId = request.getSpaceId();
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new SpaceNotFoundException("Space with ID %dl not found".formatted(spaceId), spaceId));

        Long buildingId = request.getBuildingId();
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new BuildingNotFoundException("Building with ID %dl not found".formatted(buildingId), buildingId));

        Equipment equipment = equipmentMapper.toEntity(request, building, space);
        equipment = equipmentRepository.save(equipment);
        return equipmentMapper.toDto(equipment);
    }

    @Override
    public EquipmentDto updateEquipment(long id, EquipmentUpdateDto request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ReservableNotFoundException("Equipment with ID %dl not found".formatted(id), id));

        Long spaceId = request.getSpaceId();
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new SpaceNotFoundException("Space with ID %dl not found".formatted(spaceId), spaceId));

        Long buildingId = request.getBuildingId();
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new BuildingNotFoundException("Building with ID %dl not found".formatted(buildingId), buildingId));

        equipmentMapper.updateEntityFromDto(request, building, space,  equipment);
        equipment = equipmentRepository.save(equipment);
        return equipmentMapper.toDto(equipment);
    }

    @Override
    public void deactivateEquipment(long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new ReservableNotFoundException("Equipment with ID %dl not found".formatted(id), id);
        }
        equipmentRepository.deleteById(id);
    }

    @Override
    public void activateEquipment(long id) {
        int updated = equipmentRepository.restore(id);
        if (updated == 0) {
            throw new ReservableNotFoundException("Equipment with ID %dl not found or already active".formatted(id), id);
        }
    }
}
