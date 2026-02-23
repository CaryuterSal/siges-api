package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceUpdateDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SpaceService {

    SpaceDto getSpaceById(long id);
    List<SpaceDto> searchSpacesByFilter(String searchQuery, Pageable pageable, ReservableStatus statusFilter, Long buildingIdFilter, Boolean studentsAvailableFilter, Long spaceTypeIdFilter, Boolean onlyActiveFilter);
    SpaceDto registerSpace(SpaceRegisterDto request);
    SpaceDto updateSpace(long id, SpaceUpdateDto request);
    void deactivateSpace(long id);
    void activateSpace(long id);
}
