package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.reservables.domain.model.EquipmentType;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EquipmentTypeMapper {

    EquipmentTypeDto toDto(EquipmentType equipmentType);

    List<EquipmentTypeDto> toDto(List<EquipmentType> equipmentTypes);

    @Mapping(target = "equipments", ignore = true)
    @Mapping(target = "spaceAssets", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    EquipmentType toEntity(EquipmentTypeRegisterDto dto);

    @Mapping(target = "equipments", ignore = true)
    @Mapping(target = "spaceAssets", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDto(EquipmentTypeUpdateDto dto, @MappingTarget EquipmentType entity);
}
