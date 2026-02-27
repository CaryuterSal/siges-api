package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.common.infrastructure.WithDeletedRecords;
import dev.spiffocode.sigesapi.reservables.application.mapper.SpaceTypeMapper;
import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceTypeService;
import dev.spiffocode.sigesapi.reservables.domain.exception.SpaceTypeNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static dev.spiffocode.sigesapi.reservables.domain.specification.SpaceTypeSpecifications.byActiveFilter;

@Service
@RequiredArgsConstructor
@Transactional
public class SpaceTypeServiceImpl implements SpaceTypeService {

    private final SpaceTypeRepository spaceTypeRepository;
    private final SpaceTypeMapper spaceTypeMapper;

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
    public List<SpaceTypeDto> getAllSpaceTypes(ShowModeFilter showMode) {
        List<SpaceType> spaceTypes = spaceTypeRepository.findAll(byActiveFilter(showMode));
        return spaceTypeMapper.toDto(spaceTypes);
    }

    @Override
    public SpaceTypeDto updateSpaceType(long id, SpaceTypeUpdateDto request) {
        SpaceType spaceType = spaceTypeRepository.findById(id)
                .orElseThrow(() -> new SpaceTypeNotFoundException("Space type with ID %dl not found".formatted(id), id));
        spaceTypeMapper.updateEntityFromDto(request, spaceType);
        spaceType = spaceTypeRepository.save(spaceType);
        return spaceTypeMapper.toDto(spaceType);
    }

    @Override
    public SpaceTypeDto registerSpaceType(SpaceTypeRegisterDto request) {
        SpaceType spaceType = spaceTypeMapper.toEntity(request);
        spaceType = spaceTypeRepository.save(spaceType);
        return spaceTypeMapper.toDto(spaceType);
    }

    @Override
    public void deactivateSpaceType(long id) {
        if (!spaceTypeRepository.existsById(id)) {
            throw new SpaceTypeNotFoundException("Space type with ID %dl not found".formatted(id), id);
        }
        spaceTypeRepository.deleteById(id);
    }

    @Override
    public void activateSpaceType(long id) {
        int updated = spaceTypeRepository.restore(id);
        if (updated == 0) {
            throw new SpaceTypeNotFoundException("Space type with ID %dl not found or already active".formatted(id), id);
        }
    }
}
