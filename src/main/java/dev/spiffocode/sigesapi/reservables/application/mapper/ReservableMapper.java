package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.presentation.dto.ReservableDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { SpaceMapper.class, EquipmentMapper.class })
public interface ReservableMapper {

    default ReservableDto toDto(Reservable reservable) {
        return switch (reservable) {
            case Space space         -> spaceMapper().toDto(space);
            case Equipment equipment -> equipmentMapper().toDto(equipment);
            default -> throw new IllegalArgumentException(
                "Unknown Reservable type: " + reservable.getClass().getSimpleName()
            );
        };
    }

    SpaceMapper spaceMapper();
    EquipmentMapper equipmentMapper();
}