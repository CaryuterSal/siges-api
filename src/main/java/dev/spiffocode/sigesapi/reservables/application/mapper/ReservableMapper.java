package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.presentation.dto.ReservableDto;
import org.mapstruct.Mapper;

import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = { SpaceMapper.class, EquipmentMapper.class })
public abstract class ReservableMapper {

    @Autowired
    protected SpaceMapper spaceMapper;

    @Autowired
    protected EquipmentMapper equipmentMapper;

    public ReservableDto toDto(Reservable reservable) {
        if (reservable == null)
            return null;
        return switch (reservable) {
            case Space space -> spaceMapper.toDto(space);
            case Equipment equipment -> equipmentMapper.toDto(equipment);
            default -> throw new IllegalArgumentException(
                    "Unknown Reservable type: " + reservable.getClass().getSimpleName());
        };
    }
}