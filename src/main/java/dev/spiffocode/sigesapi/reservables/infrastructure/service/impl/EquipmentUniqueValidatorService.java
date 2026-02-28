package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.common.infrastructure.WithDeletedRecords;
import dev.spiffocode.sigesapi.reservables.domain.exception.EquipmentExistsException;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentUniqueValidatorService {

    private final EquipmentRepository equipmentRepository;

    /**
     * @throws EquipmentExistsException If equipment with inventory ID number  (DELETED or not) exists
     * @param inventoryNum register equipment inventory ID number
     */
    @WithDeletedRecords
    public void assertRegisterUnique(String inventoryNum){
        if (equipmentRepository.existsByInventoryNum(inventoryNum)) {
            throw new EquipmentExistsException("Equipment with Inventory Num '%s' already exists".formatted(inventoryNum));
        }
    }

    /**
     * @throws EquipmentExistsException If the update of the record would trigger a UniqueConstraintException
     * @param currentId ID of the record to update
     * @param inventoryNum new equipment inventory ID number
     */
    @WithDeletedRecords
    public void assertUpdateUnique(Long currentId, String inventoryNum){
        if (equipmentRepository.existsByInventoryNum(inventoryNum)) {
            throw new EquipmentExistsException("Equipment with Inventory Num '%s' already exists".formatted(inventoryNum));
        }
    }
}
