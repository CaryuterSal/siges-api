package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.common.infrastructure.persistence.WithDeletedRecords;
import dev.spiffocode.sigesapi.reservables.domain.exception.EquipmentTypeExistsException;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentTypeUniqueValidatorService {

    private final EquipmentTypeRepository equipmentTypeRepository;

    /**
     * @throws EquipmentTypeExistsException If equipment type with name (DELETED or
     *                                      not) exists
     * @param name register equipment type name
     */
    @WithDeletedRecords
    public void assertRegisterUnique(String name) {
        if (equipmentTypeRepository.existsByName(name)) {
            throw new EquipmentTypeExistsException("Equipment type with name '%s' already exists".formatted(name));
        }
    }

    /**
     * @throws EquipmentTypeExistsException If the update of the record would
     *                                      trigger a UniqueConstraintException
     * @param currentId ID of the record to update
     * @param name      new equipment type name
     */
    @WithDeletedRecords
    public void assertUpdateUnique(Long currentId, String name) {
        if (equipmentTypeRepository.existsByNameAndIdNot(name, currentId)) {
            throw new EquipmentTypeExistsException("Equipment type with name '%s' already exists".formatted(name));
        }
    }
}
