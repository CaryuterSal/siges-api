package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.common.infrastructure.persistence.WithDeletedRecords;
import dev.spiffocode.sigesapi.reservables.domain.exception.InventableExistsException;
import dev.spiffocode.sigesapi.reservables.domain.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventableUniquenessValidatorService {

    private final InventoryItemRepository inventoryItemRepository;

    /**
     * @throws InventableExistsException If Inventable with inventory ID number  (DELETED or not) exists
     * @param inventoryNum register equipment inventory ID number
     */
    @WithDeletedRecords
    public void assertRegisterUnique(String inventoryNum){
        if (inventoryItemRepository.existsByInventoryNum(inventoryNum)) {
            throw new InventableExistsException("Inventable with Inventory Num '%s' already exists".formatted(inventoryNum));
        }
    }

    /**
     * @throws InventableExistsException If the update of the record would trigger a UniqueConstraintException
     * @param currentInventoryNum Inventory num of the Inventable to update
     * @param inventoryNum new equipment inventory ID number
     */
    @WithDeletedRecords
    public void assertUpdateUnique(String currentInventoryNum, String inventoryNum){
        if (inventoryItemRepository.existsByInventoryNumAndInventoryNumNot(inventoryNum, currentInventoryNum)) {
            throw new InventableExistsException("Inventable with Inventory Num '%s' already exists".formatted(inventoryNum));
        }
    }
}
