package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.presentation.dto.*;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SpaceAssetService {

    SpaceAssetDto getSpaceAssetById(long id);

    Page<@NonNull SpaceAssetDto> searchSpaceAssetsByFilter(Pageable pageable, SpaceAssetFilter filter);

    SpaceAssetDto registerSpaceAsset(long spaceId, SpaceAssetRegisterDto request);

    SpaceAssetDto updateSpaceAsset(long id, SpaceAssetUpdateDto request);

    void deactivateSpaceAsset(long id);

    void activateSpaceAsset(long id);
}
