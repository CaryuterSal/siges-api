package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceUpdateDto;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SpaceAssetService {

    SpaceDto getSpaceAssetbyId(long id);
    Page<@NonNull SpaceDto> searchSpacesByFilter(Pageable pageable, SpaceFilter spaceFilter);
    SpaceDto registerSpace(SpaceRegisterDto request);
    SpaceDto updateSpace(long id, SpaceUpdateDto request);
    void deactivateSpace(long id);
    void activateSpace(long id);
}
