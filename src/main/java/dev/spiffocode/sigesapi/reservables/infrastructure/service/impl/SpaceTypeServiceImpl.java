package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.reservables.application.mapper.SpaceTypeMapper;
import dev.spiffocode.sigesapi.reservables.application.service.ActiveFilter;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceTypeService;
import dev.spiffocode.sigesapi.reservables.domain.exception.SpaceTypeNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SpaceTypeServiceImpl implements SpaceTypeService {

    private final SpaceTypeRepository spaceTypeRepository;
    private final SpaceTypeMapper spaceTypeMapper;

    @Override
    public SpaceTypeDto getSpaceType(long id) {
        SpaceType spaceType = spaceTypeRepository.findById(id)
                .orElseThrow(() -> new SpaceTypeNotFoundException("Space type with ID %dl not found".formatted(id), id));
        return spaceTypeMapper.toDto(spaceType);
    }

    @Override
    public List<SpaceTypeDto> getAllSpaceTypes(ActiveFilter onlyActive) {
        List<SpaceType> spaceTypes = findSpaceTypeByActive(onlyActive);
        return spaceTypeMapper.toDto(spaceTypes);
    }


    //TODO: Really use only active filter
    private List<SpaceType> findSpaceTypeByActive(ActiveFilter onlyActive) {
        return switch (onlyActive) {
            case ACTIVE ->  spaceTypeRepository.findAll();
            case INACTIVE ->  spaceTypeRepository.findAllDeleted();
            case ALL -> spaceTypeRepository.findAll();
        };
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
