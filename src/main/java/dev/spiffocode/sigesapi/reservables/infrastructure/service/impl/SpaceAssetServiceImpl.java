package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.reservables.application.service.SpaceAssetFilter;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceAssetService;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetUpdateDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SpaceAssetServiceImpl implements SpaceAssetService {

    @Override
    public SpaceAssetDto getSpaceAssetById(long id) {
        return null;
    }

    @Override
    public Page<@NonNull SpaceAssetDto> searchSpaceAssetsByFilter(Pageable pageable, SpaceAssetFilter filter) {
        return null;
    }

    @Override
    public SpaceAssetDto registerSpace(SpaceAssetRegisterDto request) {
        return null;
    }

    @Override
    public SpaceAssetDto updateSpace(long id, SpaceAssetUpdateDto request) {
        return null;
    }

    @Override
    public void deactivateSpace(long id) {

    }

    @Override
    public void activateSpace(long id) {

    }
}
