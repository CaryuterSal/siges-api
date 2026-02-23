package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.reservables.application.mapper.BuildingMapper;
import dev.spiffocode.sigesapi.reservables.application.service.BuildingService;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingUpdateDto;
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
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;
    private final BuildingMapper buildingMapper;
    private final EntityManager entityManager;

    @Override
    public BuildingDto getBuilding(long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Building not found"));
        return buildingMapper.toDto(building);
    }

    @Override
    public List<BuildingDto> getAllBuildings(boolean onlyActive) {
        List<Building> buildings;
        if (onlyActive) {
            buildings = buildingRepository.findAll();
        } else {
            // Using native query to bypass Hibernate's @SoftDelete (if it were applied)
            buildings = entityManager.createNativeQuery("SELECT * FROM buildings", Building.class).getResultList();
        }
        return buildings.stream()
                .map(buildingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BuildingDto updateBuilding(long id, BuildingUpdateDto request) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Building not found"));
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Building not found");
        }
        buildingRepository.deleteById(id);
    }

    @Override
    public void activateBuilding(long id) {
        // Since deleteById uses SoftDelete, restoring it requires a native update
        int updated = entityManager.createNativeQuery("UPDATE buildings SET deleted_at = NULL WHERE id = :id")
                .setParameter("id", id)
                .executeUpdate();
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Building not found or already active");
        }
    }
}
