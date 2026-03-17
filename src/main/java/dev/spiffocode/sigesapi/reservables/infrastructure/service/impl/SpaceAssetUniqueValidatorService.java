package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.reservables.domain.exception.InventableExistsException;
import dev.spiffocode.sigesapi.reservables.domain.exception.SpaceAssetExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpaceAssetUniqueValidatorService {

    private final InventableUniquenessValidatorService inventableValidator;

    /**
     * @throws SpaceAssetExistsException If space asset with inventory ID number  (DELETED or not) exists
     * @param inventoryNum register space asset inventory ID number
     */
    public void assertRegisterUnique(String inventoryNum){
        try {
            inventableValidator.assertRegisterUnique(inventoryNum);
        } catch (InventableExistsException e){
            throw new SpaceAssetExistsException("Space asset with Inventory Num '%s' already exists".formatted(inventoryNum));
        }
    }

    /**
     * @throws SpaceAssetExistsException If the update of the record would trigger a UniqueConstraintException
     * @param currentInventoryNum Inventory Num of the record to update
     * @param inventoryNum new space asset inventory ID number
     */
    public void assertUpdateUnique(String currentInventoryNum, String inventoryNum){
        try {
            inventableValidator.assertUpdateUnique(currentInventoryNum, inventoryNum);
        } catch (InventableExistsException e){
            throw new SpaceAssetExistsException("Space asset with Inventory Num '%s' already exists".formatted(inventoryNum));
        }
    }
}
