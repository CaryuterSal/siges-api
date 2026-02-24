package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.reservables.application.mapper.BuildingMapper;
import dev.spiffocode.sigesapi.reservables.application.service.BuildingService;
import dev.spiffocode.sigesapi.reservables.domain.exception.BuildingNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.core.support.RepositoryMethodInvocationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;
    private final BuildingMapper buildingMapper;
    private final RepositoryMethodInvocationListener repositoryMethodInvocationListener;


    @Override
    public BuildingDto getBuilding(long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new BuildingNotFoundException("Building with ID %dl not found".formatted(id), id));
        return buildingMapper.toDto(building);
    }

    @Override
    public List<BuildingDto> getAllBuildings(boolean onlyActive) {
        List<Building> buildings = findBuildingByActive(onlyActive);

        return buildingMapper.toDto(buildings);
    }

    private List<Building> findBuildingByActive(boolean onlyActive) {
        return onlyActive? buildingRepository.findAll() : buildingRepository.findAllDeleted();
    }

    @Override
    public BuildingDto updateBuilding(long id, BuildingUpdateDto request) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new BuildingNotFoundException("Building with ID %dl not found".formatted(id), id));
        buildingMapper.updateEntityFromDto(request, building);
        building = buildingRepository.save(building);
        return buildingMapper.toDto(building);
    }

    @Override
    public BuildingDto registerBuilding(BuildingRegisterDto request) {
        Building building = buildingMapper.toEntity(request);
        building = buildingRepository.save(building);
        return buildingMapper.toDto(building);
    }

    @Override
    public void deactivateBuilding(long id) {
        if (!buildingRepository.existsById(id)) {
            throw new BuildingNotFoundException("Building with ID %dl not found".formatted(id), id);
        }
        buildingRepository.deleteById(id);
    }

    @Override
    public void activateBuilding(long id) {
        int updated = buildingRepository.restore(id);
        if (updated == 0) {
            throw new BuildingNotFoundException("Building with ID %dl not found or already active".formatted(id), id);
        }
    }
}
