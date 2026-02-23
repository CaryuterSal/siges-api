package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { BuildingMapper.class, SpaceTypeMapper.class, AvailabilityMapper.class })
public interface SpaceMapper {

    @Mapping(target = "spaceType", source = "type")
    @Mapping(target = "bookInAdvanceDuration", source = "bookInAdvance")
    @Mapping(target = "availableForStudents", source = "studentsAvailable")
    SpaceDto toDto(Space space);

    @Mapping(target = "type", source = "spaceType")
    @Mapping(target = "bookInAdvance", source = "bookInAdvanceDuration")
    @Mapping(target = "building", source = "building")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Space toEntity(SpaceRegisterDto dto, SpaceType spaceType, Building building);

    @Mapping(target = "type", source = "type")
    @Mapping(target = "bookInAdvance", source = "bookInAdvanceDuration")
    @Mapping(target = "building", source = "building")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDto(SpaceUpdateDto dto, SpaceType type, Building building, @MappingTarget Space entity);
}
