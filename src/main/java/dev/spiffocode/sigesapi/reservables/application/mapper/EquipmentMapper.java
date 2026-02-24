package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = { BuildingMapper.class, SpaceMapper.class, AvailabilityMapper.class })
public interface EquipmentMapper {

    @Mapping(target = "spaceAttached", source = "space")
    @Mapping(target = "inventoryIdNum", source = "inventoryNum")
    @Mapping(target = "availableForStudents", source = "studentsAvailable")
    @Mapping(target = "availabilitySlots", source = "availability")
    EquipmentDto toDto(Equipment equipment);

    @Mapping(target = "space", source = "space")
    @Mapping(target = "building", source = "building")
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "status", source = "dto.status")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "studentsAvailable", source = "dto.studentsAvailable")
    @Mapping(target = "availability", source = "dto.availability")
    @Mapping(target = "availabilityExceptions", source = "dto.exceptions")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Equipment toEntity(EquipmentRegisterDto dto, Building building, Space space);

    @Mapping(target = "space", source = "space")
    @Mapping(target = "building", source = "building")
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "status", source = "dto.status")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "studentsAvailable", source = "dto.studentsAvailable")
    @Mapping(target = "availability", ignore = true)
    @Mapping(target = "availabilityExceptions", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDto(EquipmentUpdateDto dto, Building building, Space space, @MappingTarget Equipment entity);
}
