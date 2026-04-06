package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.common.infrastructure.exceptions.ConflictingStateException;
import dev.spiffocode.sigesapi.common.infrastructure.persistence.WithDeletedRecords;
import dev.spiffocode.sigesapi.reservables.application.mapper.SpaceTypeMapper;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceTypeFilter;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceTypeService;
import dev.spiffocode.sigesapi.reservables.domain.exception.SpaceTypeNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import dev.spiffocode.sigesapi.reservables.domain.specification.SpaceTypeSpecifications;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SpaceTypeServiceImpl implements SpaceTypeService {

    private final SpaceTypeRepository spaceTypeRepository;
    private final SpaceRepository spaceRepository;
    private final SpaceTypeMapper spaceTypeMapper;
    private final SpaceTypeUniqueValidatorService uniqueValidator;

    @PostAuthorize("!hasRole('APPLICANT') or returnObject.deletedAt == null")
    @WithDeletedRecords
    @Override
    public SpaceTypeDto getSpaceType(long id) {
        SpaceType spaceType = spaceTypeRepository.findById(id)
                .orElseThrow(() -> new SpaceTypeNotFoundException("Space type with ID %dl not found".formatted(id), id));
        return spaceTypeMapper.toDto(spaceType);
    }

    @WithDeletedRecords
    @Override
    public List<SpaceTypeDto> getAllSpaceTypes(SpaceTypeFilter filter) {
        List<SpaceType> spaceTypes = spaceTypeRepository.findAll(SpaceTypeSpecifications.byFilter(filter));
        return spaceTypeMapper.toDto(spaceTypes);
    }

    @Override
    public SpaceTypeDto updateSpaceType(long id, SpaceTypeUpdateDto request) {
        SpaceType spaceType = spaceTypeRepository.findById(id)
                .orElseThrow(() -> new SpaceTypeNotFoundException("Space type with ID %dl not found".formatted(id), id));

        uniqueValidator.assertUpdateUnique(spaceType.getId(), request.name());

        spaceTypeMapper.updateEntityFromDto(request, spaceType);
        spaceType = spaceTypeRepository.save(spaceType);
        return spaceTypeMapper.toDto(spaceType);
    }

    @Override
    public SpaceTypeDto registerSpaceType(SpaceTypeRegisterDto request) {
        uniqueValidator.assertRegisterUnique(request.getName());

        SpaceType spaceType = spaceTypeMapper.toEntity(request);
        spaceType = spaceTypeRepository.save(spaceType);
        return spaceTypeMapper.toDto(spaceType);
    }

    @Override
    public void deactivateSpaceType(long id) {
        SpaceType spaceType = spaceTypeRepository.findById(id)
                .orElseThrow(() -> new SpaceTypeNotFoundException("Space type with ID %dl not found".formatted(id), id));

        if(spaceRepository.existsByTypeId(spaceType.getId())){
            throw new ConflictingStateException("Cannot deactivate Space Type. Still have spaces linked to. Either deactivate those spaces or re-assign them to other space type");
        }
        spaceTypeRepository.softDeleteById(id);
    }

    @Override
    public void activateSpaceType(long id) {
        int updated = spaceTypeRepository.restore(id);
        if (updated == 0) {
            throw new SpaceTypeNotFoundException("Space type with ID %dl not found or already active".formatted(id), id);
        }
    }
}
