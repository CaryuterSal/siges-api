package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.reservables.application.mapper.SpaceMapper;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceService;
import dev.spiffocode.sigesapi.reservables.domain.exception.ReservableNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceUpdateDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dev.spiffocode.sigesapi.common.domain.specification.SpecificationHelper.cast;
import static dev.spiffocode.sigesapi.reservables.domain.specification.EquipmentSpecifications.inSpace;
import static dev.spiffocode.sigesapi.reservables.domain.specification.EquipmentSpecifications.inventoryNumContains;
import static dev.spiffocode.sigesapi.reservables.domain.specification.ReservableSpecifications.*;

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
    public Page<@NonNull SpaceDto> searchSpacesByFilter(String searchQuery,
                                                        Pageable pageable,
                                                        ReservableStatus statusFilter,
                                                        Long buildingIdFilter,
                                                        Boolean studentsAvailableFilter,
                                                        Long spaceTypeIdFilter,
                                                        Boolean onlyActiveFilter) {

        Specification<@NonNull Equipment> spec = Specification
                .where(inSpace(spaceIdFilter))
                .and(cast(statusIs(statusFilter)))
                .and(
                        inventoryNumContains(searchQuery)
                                .or(cast(descriptionContains(searchQuery)))
                                .or(cast(nameContains(searchQuery)))
                )
                .and(cast(inBuilding(buildingIdFilter)))
                .and(cast(availableForStudents(studentsAvailableFilter)));
        spaceRepository.finda
        Specification<Space> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (searchQuery != null && !searchQuery.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + searchQuery.toLowerCase() + "%"));
            }
            if (statusFilter != null) {
                predicates.add(cb.equal(root.get("status"), statusFilter));
            }
            if (buildingIdFilter != null) {
                predicates.add(cb.equal(root.get("building").get("id"), buildingIdFilter));
            }
            if (studentsAvailableFilter != null) {
                predicates.add(cb.equal(root.get("studentsAvailable"), studentsAvailableFilter));
            }
            if (spaceTypeIdFilter != null) {
                predicates.add(cb.equal(root.get("type").get("id"), spaceTypeIdFilter));
            }

            // Note: onlyActiveFilter = false means "Include deleted".
            // In standard Spring Data JPA + Hibernate @SoftDelete, there is no simple way
            // to disable the filter per query cleanly without native queries.
            // Assuming @SoftDelete handles the active filtering for us by default.
            // If onlyActiveFilter == false, this naive Specification will still omit
            // deleted items.

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return spaceRepository.findAll(spec, pageable).getContent().stream()
                .map(spaceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SpaceDto registerSpace(SpaceRegisterDto request) {
        if (!spaceTypeRepository.existsById(request.getSpaceTypeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SpaceType does not exist");
        }
        if (!buildingRepository.existsById(request.getBuildingId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Building does not exist");
        }

        Space space = spaceMapper.toEntity(request);
        space = spaceRepository.save(space);
        return spaceMapper.toDto(space);
    }

    @Override
    public SpaceDto updateSpace(long id, SpaceUpdateDto request) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Space not found"));

        if (!spaceTypeRepository.existsById(request.getSpaceTypeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SpaceType does not exist");
        }
        if (!buildingRepository.existsById(request.getBuildingId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Building does not exist");
        }

        spaceMapper.updateEntityFromDto(request, space);
        space = spaceRepository.save(space);
        return spaceMapper.toDto(space);
    }

    @Override
    public void deactivateSpace(long id) {
        if (!spaceRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Space not found");
        }
        spaceRepository.deleteById(id);
    }

    @Override
    public void activateSpace(long id) {
        int updated = entityManager.createNativeQuery("UPDATE reservables SET deleted_at = NULL WHERE id = :id")
                .setParameter("id", id)
                .executeUpdate();
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Space not found or already active");
        }
    }
}
