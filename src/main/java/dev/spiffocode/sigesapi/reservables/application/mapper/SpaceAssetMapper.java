package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.reservables.domain.model.*;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetUpdateDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring", uses = { BuildingMapper.class, SpaceMapper.class, EquipmentTypeMapper.class, InventoryItemMapper.class})
public interface SpaceAssetMapper {

    @Mapping(target = "inventoryIdNum", source = "inventoryItem.inventoryNum")
    @Mapping(target = "availableForStudents", source = "studentsAvailable")
    @Mapping(target = "availabilitySlots", source = "availability")
    SpaceAssetDto toDto(SpaceAsset equipment);


    @Mapping(target = "inventoryItem", source = "inventoryNum")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "space", source = "space")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    SpaceAsset toEntity(SpaceAssetRegisterDto dto, Space space, EquipmentType type);


    @Mapping(target = "inventoryItem", source = "inventoryNum")
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "type", source = "type")
    @Mapping(target = "name", source = "dto.name")
    @Mapping(target = "description", source = "dto.description")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntityFromDto(SpaceAssetUpdateDto dto, EquipmentType type,
                             @MappingTarget SpaceAsset entity);

    @AfterMapping
    default void linkRelations(@MappingTarget SpaceAsset asset,
                               Space space,
                               EquipmentType type) {

        if (space != null) {
            asset.attachSpace(space);
        }

        if (type != null) {
            type.addSpaceAsset(asset);
        }
    }
}
