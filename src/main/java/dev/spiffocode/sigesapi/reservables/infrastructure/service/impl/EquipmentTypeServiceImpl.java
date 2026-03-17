package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;
import dev.spiffocode.sigesapi.common.infrastructure.persistence.WithDeletedRecords;
import dev.spiffocode.sigesapi.reservables.application.mapper.EquipmentTypeMapper;
import dev.spiffocode.sigesapi.reservables.application.service.EquipmentTypeFilter;
import dev.spiffocode.sigesapi.reservables.application.service.EquipmentTypeService;
import dev.spiffocode.sigesapi.reservables.domain.exception.EquipmentTypeNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.EquipmentType;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentTypeRepository;
import dev.spiffocode.sigesapi.reservables.domain.specification.EquipmentTypeSpecifications;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentTypeServiceImpl implements EquipmentTypeService {

    private final EquipmentTypeRepository equipmentTypeRepository;
    private final EquipmentTypeMapper equipmentTypeMapper;
    private final EquipmentTypeUniqueValidatorService uniqueValidator;

    @PostAuthorize("!hasRole('APPLICANT') or returnObject.deletedAt == null")
    @WithDeletedRecords
    @Override
    public EquipmentTypeDto getEquipmentType(long id) {
        EquipmentType equipmentType = equipmentTypeRepository.findById(id)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(
                        "Equipment type with ID %dl not found".formatted(id), id));
        return equipmentTypeMapper.toDto(equipmentType);
    }

    @WithDeletedRecords
    @Override
    public List<EquipmentTypeDto> getAllEquipmentTypes(EquipmentTypeFilter filter) {
        List<EquipmentType> equipmentTypes = equipmentTypeRepository.findAll(EquipmentTypeSpecifications.byFilter(filter));
        return equipmentTypeMapper.toDto(equipmentTypes);
    }

    @Override
    public EquipmentTypeDto updateEquipmentType(long id, EquipmentTypeUpdateDto request) {
        EquipmentType equipmentType = equipmentTypeRepository.findById(id)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(
                        "Equipment type with ID %dl not found".formatted(id), id));

        uniqueValidator.assertUpdateUnique(equipmentType.getId(), request.name());

        equipmentTypeMapper.updateEntityFromDto(request, equipmentType);
        equipmentType = equipmentTypeRepository.save(equipmentType);
        return equipmentTypeMapper.toDto(equipmentType);
    }

    @Override
    public EquipmentTypeDto registerEquipmentType(EquipmentTypeRegisterDto request) {
        uniqueValidator.assertRegisterUnique(request.getName());

        EquipmentType equipmentType = equipmentTypeMapper.toEntity(request);
        equipmentType = equipmentTypeRepository.save(equipmentType);
        return equipmentTypeMapper.toDto(equipmentType);
    }

    @Override
    public void deactivateEquipmentType(long id) {
        EquipmentType equipmentType = equipmentTypeRepository.findById(id)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(
                        "Equipment type with ID %dl not found".formatted(id), id));

        if (equipmentType.getEquipments() != null && !equipmentType.getEquipments().isEmpty()) {
            throw new ConflictingStateException(
                    "Cannot deactivate Equipment Type. Still have equipments linked to. Either deactivate those equipments or re-assign them to other equipment type");
        }
        equipmentTypeRepository.softDeleteById(id);
    }

    @Override
    public void activateEquipmentType(long id) {
        int updated = equipmentTypeRepository.restore(id);
        if (updated == 0) {
            throw new EquipmentTypeNotFoundException(
                    "Equipment type with ID %dl not found or already active".formatted(id), id);
        }
    }
}
