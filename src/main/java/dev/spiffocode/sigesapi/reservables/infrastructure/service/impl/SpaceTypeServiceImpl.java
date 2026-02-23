package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.reservables.application.mapper.SpaceTypeMapper;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceTypeService;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeUpdateDto;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SpaceTypeServiceImpl implements SpaceTypeService {

    private final SpaceTypeRepository spaceTypeRepository;
    private final SpaceTypeMapper spaceTypeMapper;
    private final EntityManager entityManager;

    @Override
    public SpaceTypeDto getSpaceType(long id) {
        SpaceType spaceType = spaceTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SpaceType not found"));
        return spaceTypeMapper.toDto(spaceType);
    }

    @Override
    public List<SpaceTypeDto> getAllSpaceTypes(boolean onlyActive) {
        List<SpaceType> spaceTypes;
        if (onlyActive) {
            spaceTypes = spaceTypeRepository.findAll();
        } else {
            spaceTypes = entityManager.createNativeQuery("SELECT * FROM space_types", SpaceType.class).getResultList();
        }
        return spaceTypes.stream()
                .map(spaceTypeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SpaceTypeDto updateSpaceType(long id, SpaceTypeUpdateDto request) {
        SpaceType spaceType = spaceTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SpaceType not found"));
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SpaceType not found");
        }
        spaceTypeRepository.deleteById(id);
    }

    @Override
    public void activateSpaceType(long id) {
        int updated = entityManager.createNativeQuery("UPDATE space_types SET deleted_at = NULL WHERE id = :id")
                .setParameter("id", id)
                .executeUpdate();
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SpaceType not found or already active");
        }
    }
}
