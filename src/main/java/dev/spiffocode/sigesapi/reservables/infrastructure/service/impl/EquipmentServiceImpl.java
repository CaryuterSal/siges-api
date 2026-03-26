package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.auth.infrastructure.SecurityContextHelper;
import dev.spiffocode.sigesapi.common.infrastructure.persistence.WithDeletedRecords;
import dev.spiffocode.sigesapi.reservables.application.mapper.EquipmentMapper;
import dev.spiffocode.sigesapi.reservables.application.service.EquipmentFilter;
import dev.spiffocode.sigesapi.reservables.application.service.EquipmentService;
import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.domain.exception.BuildingNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.exception.EquipmentTypeNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.exception.ReservableNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.exception.SpaceNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import dev.spiffocode.sigesapi.reservables.domain.model.EquipmentType;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentTypeRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentUpdateDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static dev.spiffocode.sigesapi.common.domain.specification.SpecificationHelper.cast;
import static dev.spiffocode.sigesapi.reservables.domain.specification.EquipmentSpecifications.equipmentSpecification;
import static dev.spiffocode.sigesapi.reservables.domain.specification.ReservableSpecifications.availableForStudents;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentMapper equipmentMapper;
    private final SpaceRepository spaceRepository;
    private final BuildingRepository buildingRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final EquipmentUniqueValidatorService uniqueValidator;
    private final SecurityContextHelper securityContextHelper;

    @PostAuthorize("!hasRole('APPLICANT') or returnObject.deletedAt == null")
    @WithDeletedRecords
    @Override
    public EquipmentDto getEquipmentById(long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(
                        () -> new ReservableNotFoundException("Equipment with ID %dl not found".formatted(id), id));
        return equipmentMapper.toDto(equipment);
    }

    @WithDeletedRecords
    @Override
    public Page<@NonNull EquipmentDto> searchEquipmentsByFilter(Pageable pageable, EquipmentFilter filter) {
        return equipmentRepository.findAll(resolveSpecification(filter), pageable)
                .map(equipmentMapper::toDto);
    }

    private Specification<@NonNull Equipment> resolveSpecification(EquipmentFilter filter) {

        EquipmentFilter actualFilter = securityContextHelper.isAdmin() ? filter : filter.withShowModeFilter(ShowModeFilter.ACTIVE);
        Specification<@NonNull Equipment> spec = equipmentSpecification(filter);

        if (securityContextHelper.isStudent())
            return spec.and(cast(availableForStudents(true)));
        return spec;
    }

    @Override
    public EquipmentDto registerEquipment(EquipmentRegisterDto request) {
        Space space = findSpace(request.getSpaceId());
        Building building = findBuilding(request.getBuildingId());
        EquipmentType type = findEquipmentType(request.getEquipmentTypeId());

        uniqueValidator.assertRegisterUnique(request.getInventoryNum());

        Equipment equipment = equipmentMapper.toEntity(request, building, space, type);
        equipment = equipmentRepository.save(equipment);
        return equipmentMapper.toDto(equipment);
    }

    @Override
    public EquipmentDto updateEquipment(long id, EquipmentUpdateDto request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(
                        () -> new ReservableNotFoundException("Equipment with ID %dl not found".formatted(id), id));

        Space space = findSpace(request.getSpaceId());
        Building building = findBuilding(request.getBuildingId());
        EquipmentType type = findEquipmentType(request.getEquipmentTypeId());

        uniqueValidator.assertUpdateUnique(equipment.getInventoryNum(), request.getInventoryNum());

        equipmentMapper.updateEntityFromDto(request, building, space, type, equipment);
        equipment = equipmentRepository.save(equipment);
        return equipmentMapper.toDto(equipment);
    }

    public Building findBuilding(Long id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new BuildingNotFoundException("Building with ID %dl not found".formatted(id), id));

    }

    public Space findSpace(Long id) {
        return Optional.ofNullable(id)
                .map(actualId -> spaceRepository.findById(actualId)
                        .orElseThrow(() -> new SpaceNotFoundException("Space with ID %dl not found".formatted(actualId),
                                actualId)))
                .orElse(null);
    }

    public EquipmentType findEquipmentType(Long id) {
        return equipmentTypeRepository.findById(id)
                        .orElseThrow(() -> new EquipmentTypeNotFoundException(
                                "Equipment type with ID %dl not found".formatted(id), id));
    }

    @Override
    public void deactivateEquipment(long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new ReservableNotFoundException("Equipment with ID %dl not found".formatted(id), id);
        }
        equipmentRepository.softDeleteById(id);
    }

    @Override
    public void activateEquipment(long id) {
        int updated = equipmentRepository.restore(id);
        if (updated == 0) {
            throw new ReservableNotFoundException("Equipment with ID %dl not found or already active".formatted(id),
                    id);
        }
    }
}
