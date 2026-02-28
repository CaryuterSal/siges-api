package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.common.infrastructure.WithDeletedRecords;
import dev.spiffocode.sigesapi.reservables.domain.exception.BuildingExistsException;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BuildingUniqueValidatorService {

    private final BuildingRepository buildingRepository;

    /**
     * @throws BuildingExistsException If building with name  (DELETED or not) exists
     * @param name register building name
     */
    @WithDeletedRecords
    public void assertRegisterUnique(String name){
        if(buildingRepository.existsByName(name)){
            throw new BuildingExistsException("Building with name '%s' already exists".formatted(name));
        }
    }

    /**
     * @throws BuildingExistsException If the update of the record would trigger a UniqueConstraintException
     * @param currentId ID of the record to update
     * @param name new building name
     */
    @WithDeletedRecords
    public void assertUpdateUnique(Long currentId, String name){
        if(buildingRepository.existsByNameAndIdNot(name, currentId)){
            throw new BuildingExistsException("Building with name '%s' already exists".formatted(name));
        }
    }
}
