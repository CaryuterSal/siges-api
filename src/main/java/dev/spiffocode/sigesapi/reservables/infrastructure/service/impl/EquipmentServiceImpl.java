package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.reservables.application.mapper.EquipmentMapper;
import dev.spiffocode.sigesapi.reservables.application.service.EquipmentService;
import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentUpdateDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentMapper equipmentMapper;
    private final SpaceRepository spaceRepository;
    private final BuildingRepository buildingRepository;
    private final EntityManager entityManager;

    @Override
    public EquipmentDto getEquipmentById(long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found"));
        return equipmentMapper.toDto(equipment);
    }

    @Override
    public List<EquipmentDto> searchEquipmentsByFilter(String searchQuery, Pageable pageable,
            ReservableStatus statusFilter, Long buildingIdFilter, Boolean studentsAvailableFilter, Long spaceIdFilter,
            Boolean onlyActiveFilter) {
        Specification<Equipment> spec = (root, query, cb) -> {
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
            if (spaceIdFilter != null) {
                predicates.add(cb.equal(root.get("space").get("id"), spaceIdFilter));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return equipmentRepository.findAll(spec, pageable).getContent().stream()
                .map(equipmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EquipmentDto registerEquipment(EquipmentRegisterDto request) {
        if (request.getSpaceId() != null && !spaceRepository.existsById(request.getSpaceId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Space does not exist");
        }
        if (!buildingRepository.existsById(request.getBuildingId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Building does not exist");
        }

        Equipment equipment = equipmentMapper.toEntity(request);
        equipment = equipmentRepository.save(equipment);
        return equipmentMapper.toDto(equipment);
    }

    @Override
    public EquipmentDto updateEquipment(long id, EquipmentUpdateDto request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found"));

        if (request.getSpaceId() != null && !spaceRepository.existsById(request.getSpaceId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Space does not exist");
        }
        if (!buildingRepository.existsById(request.getBuildingId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Building does not exist");
        }

        equipmentMapper.updateEntityFromDto(request, equipment);
        equipment = equipmentRepository.save(equipment);
        return equipmentMapper.toDto(equipment);
    }

    @Override
    public void deactivateEquipment(long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found");
        }
        equipmentRepository.deleteById(id);
    }

    @Override
    public void activateEquipment(long id) {
        int updated = entityManager.createNativeQuery("UPDATE reservables SET deleted_at = NULL WHERE id = :id")
                .setParameter("id", id)
                .executeUpdate();
        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipment not found or already active");
        }
    }
}
