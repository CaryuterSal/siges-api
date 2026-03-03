package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.common.infrastructure.persistence.WithDeletedRecords;
import dev.spiffocode.sigesapi.reservables.domain.exception.SpaceTypeExistsException;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SpaceTypeUniqueValidatorService {

    private final SpaceTypeRepository spaceTypeRepository;

    /**
     * @throws SpaceTypeExistsException If space type with name (DELETED or not) exists
     * @param name register space type name
     */
    @WithDeletedRecords
    public void assertRegisterUnique(String name){
        if(spaceTypeRepository.existsByName(name)){
            throw new SpaceTypeExistsException("Space type with name '%s' already exists".formatted(name));
        }
    }

    /**
     * @throws SpaceTypeExistsException If the update of the record would trigger a UniqueConstraintException
     * @param currentId ID of the record to update
     * @param name new space type name
     */
    @WithDeletedRecords
    public void assertUpdateUnique(Long currentId, String name){
        if(spaceTypeRepository.existsByNameAndIdNot(name, currentId)){
            throw new SpaceTypeExistsException("Space type with name '%s' already exists".formatted(name));
        }
    }
}
