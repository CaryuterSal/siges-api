package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.reservables.application.mapper.SpaceMapper;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceFilter;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceService;
import dev.spiffocode.sigesapi.reservables.domain.exception.BuildingNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.exception.ReservableNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.exception.SpaceNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.exception.SpaceTypeNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceUpdateDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dev.spiffocode.sigesapi.reservables.domain.specification.SpaceSpecifications.spaceSpecification;

@Service
@RequiredArgsConstructor
@Transactional
public class SpaceServiceImpl implements SpaceService {

    private final SpaceRepository spaceRepository;
    private final SpaceMapper spaceMapper;
    private final SpaceTypeRepository spaceTypeRepository;
    private final BuildingRepository buildingRepository;

    @Override
    public SpaceDto getSpaceById(long id) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new ReservableNotFoundException("Space with ID %dl not found".formatted(id), id));
        return spaceMapper.toDto(space);
    }

    @Override
    public Page<@NonNull SpaceDto> searchSpacesByFilter(Pageable pageable, SpaceFilter filter) {
        return spaceRepository.findAll(spaceSpecification(filter), pageable)
                .map(spaceMapper::toDto);
    }

    @Override
    public SpaceDto registerSpace(SpaceRegisterDto request) {
        Long spaceTypeId =  request.getSpaceTypeId();
        SpaceType spaceType = spaceTypeRepository.findById(spaceTypeId)
                .orElseThrow(() -> new SpaceTypeNotFoundException("Space Type with ID %dl not found".formatted(spaceTypeId), spaceTypeId));

        Long buildingId = request.getBuildingId();
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new BuildingNotFoundException("Building with ID %dl not found".formatted(buildingId), buildingId));

        Space space = spaceMapper.toEntity(request, spaceType, building);
        space = spaceRepository.save(space);
        return spaceMapper.toDto(space);
    }

    @Override
    public SpaceDto updateSpace(long id, SpaceUpdateDto request) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new SpaceNotFoundException("Space with ID %dl not found".formatted(id), id));


        Long spaceTypeId =  request.getSpaceTypeId();
        SpaceType spaceType = spaceTypeRepository.findById(spaceTypeId)
                .orElseThrow(() -> new SpaceTypeNotFoundException("Space Type with ID %dl not found".formatted(spaceTypeId), spaceTypeId));

        Long buildingId = request.getBuildingId();
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new BuildingNotFoundException("Building with ID %dl not found".formatted(buildingId), buildingId));

        spaceMapper.updateEntityFromDto(request, spaceType, building, space);
        space = spaceRepository.save(space);
        return spaceMapper.toDto(space);
    }

    @Override
    public void deactivateSpace(long id) {
        if (!spaceRepository.existsById(id)) {
            throw new SpaceNotFoundException("Space with ID %dl not found".formatted(id), id);
        }
        spaceRepository.deleteById(id);
    }

    @Override
    public void activateSpace(long id) {
        int updated = spaceRepository.restore(id);
        if (updated == 0) {
            throw new SpaceNotFoundException("Space with ID %dl not found".formatted(id), id);
        }
    }
}
