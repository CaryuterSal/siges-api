package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.presentation.dto.ReservableDto;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = { SpaceMapper.class, EquipmentMapper.class })
public abstract class ReservableMapper {

    protected SpaceMapper spaceMapper;
    protected EquipmentMapper equipmentMapper;

    @Autowired
    public void setSpaceMapper(SpaceMapper spaceMapper) {
        this.spaceMapper = spaceMapper;
    }

    @Autowired
    public void setEquipmentMapper(EquipmentMapper equipmentMapper) {
        this.equipmentMapper = equipmentMapper;
    }

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

    static void linkRelations(Reservable reservable){

        if (reservable.getAvailability() != null && !reservable.getAvailability().isEmpty()) {
            reservable.getAvailability().forEach(av -> av.setReservable(reservable));
        }

        if (reservable.getAvailabilityExceptions() != null && !reservable.getAvailabilityExceptions().isEmpty()) {
            reservable.getAvailabilityExceptions().forEach(av -> av.setReservable(reservable));
        }
    }
}