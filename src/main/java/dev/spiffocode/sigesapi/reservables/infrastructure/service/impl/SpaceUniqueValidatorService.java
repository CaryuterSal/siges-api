package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.common.infrastructure.WithDeletedRecords;
import dev.spiffocode.sigesapi.reservables.domain.exception.SpaceExistsException;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SpaceUniqueValidatorService {

    private final SpaceRepository spaceRepository;

    /**
     * @throws SpaceExistsException If space with name located in building (DELETED or not) exists
     * @param name register space name
     * @param building register space location
     */
    @WithDeletedRecords
    public void assertRegisterUnique(String name, Building building){
        if(spaceRepository.existsByNameAndBuilding(name, building)){
            throw new SpaceExistsException("Space with name '%s' in building '%s' already exists".formatted(name, building.getName()));
        }
    }

    /**
     * @throws SpaceExistsException If the update of the record would trigger a UniqueConstraintException
     * @param currentId ID of the record to update
     * @param name new space name
     * @param building register space location
     */
    @WithDeletedRecords
    public void assertUpdateUnique(Long currentId, String name, Building building){
        if(spaceRepository.existsByNameAndBuildingAndIdNot(name, building, currentId)){
            throw new SpaceExistsException("Space with name '%s' in building '%s' already exists".formatted(name, building.getName()));
        }
    }
}
