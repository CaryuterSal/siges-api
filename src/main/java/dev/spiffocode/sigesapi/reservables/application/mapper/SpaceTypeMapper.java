package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpaceTypeMapper {

    SpaceTypeDto toDto(SpaceType spaceType);

    List<SpaceTypeDto> toDto(List<SpaceType> spaceType);

    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    SpaceType toEntity(SpaceTypeRegisterDto dto);

    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(SpaceTypeUpdateDto dto, @MappingTarget SpaceType entity);
}
