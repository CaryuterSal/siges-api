package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.auth.infrastructure.SecurityContextHelper;
import dev.spiffocode.sigesapi.common.infrastructure.persistence.WithDeletedRecords;
import dev.spiffocode.sigesapi.reservables.application.mapper.SpaceAssetMapper;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceAssetFilter;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceAssetService;
import dev.spiffocode.sigesapi.reservables.domain.exception.EquipmentTypeNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.exception.SpaceAssetNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.exception.SpaceNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.EquipmentType;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceAsset;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentTypeRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceAssetRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.domain.specification.SpaceAssetSpecifications;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetUpdateDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpaceAssetServiceImpl implements SpaceAssetService {

    private final SpaceRepository spaceRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final SpaceAssetRepository assetRepository;
    private final SpaceAssetUniqueValidatorService uniqueValidatorService;
    private final SecurityContextHelper securityContextHelper;
    private final SpaceAssetMapper mapper;

    @PostAuthorize("!hasRole('APPLICANT') or returnObject.deletedAt == null")
    @WithDeletedRecords
    @Override
    public SpaceAssetDto getSpaceAssetById(long id) {
        SpaceAsset asset = assetRepository.findById(id)
                .orElseThrow(() -> new SpaceAssetNotFoundException(id));
        return mapper.toDto(asset);
    }

    @WithDeletedRecords
    @Override
    public Page<@NonNull SpaceAssetDto> searchSpaceAssetsByFilter(Pageable pageable, SpaceAssetFilter filter) {
        return assetRepository.findAll(SpaceAssetSpecifications.byFilter(filter), pageable)
                .map(mapper::toDto);
    }

    @Override
    public SpaceAssetDto registerSpaceAsset(long spaceId, SpaceAssetRegisterDto request) {
        Space space = findSpace(spaceId);
        EquipmentType type = findEquipmentType(request.typeId());

        uniqueValidatorService.assertRegisterUnique(request.inventoryNum());

        SpaceAsset spaceAsset = mapper.toEntity(request, space, type);
        spaceAsset = assetRepository.save(spaceAsset);
        return mapper.toDto(spaceAsset);
    }

    @Override
    public SpaceAssetDto updateSpaceAsset(long id, SpaceAssetUpdateDto request) {
        SpaceAsset spaceAsset = assetRepository.findById(id)
                .orElseThrow(
                        () -> new SpaceAssetNotFoundException(id));

        EquipmentType type = findEquipmentType(request.typeId());

        uniqueValidatorService.assertUpdateUnique(spaceAsset.getInventoryNum(), request.inventoryNum());

        mapper.updateEntityFromDto(request, type, spaceAsset);
        spaceAsset = assetRepository.save(spaceAsset);
        return mapper.toDto(spaceAsset);
    }

    @Override
    public void deactivateSpaceAsset(long id) {
        if (!assetRepository.existsById(id)) {
            throw new SpaceAssetNotFoundException(id);
        }
        assetRepository.softDeleteById(id);
    }

    @Override
    public void activateSpaceAsset(long id) {
        int updated = assetRepository.restore(id);
        if (updated == 0) {
            throw new SpaceAssetNotFoundException(id);
        }
    }


    private Space findSpace(Long id) {
        return Optional.ofNullable(id)
                .map(actualId -> spaceRepository.findById(actualId)
                        .orElseThrow(() -> new SpaceNotFoundException("Space with ID %dl not found".formatted(actualId),
                                actualId)))
                .orElse(null);
    }

    private EquipmentType findEquipmentType(Long id) {
        return equipmentTypeRepository.findById(id)
                        .orElseThrow(() -> new EquipmentTypeNotFoundException(
                                "Equipment type with ID %dl not found".formatted(id), id));
    }
}
