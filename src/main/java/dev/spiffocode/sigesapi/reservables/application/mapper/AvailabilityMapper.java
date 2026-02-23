package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.reservables.domain.model.Availability;
import dev.spiffocode.sigesapi.reservables.domain.model.AvailabilitySlot;
import dev.spiffocode.sigesapi.reservables.presentation.dto.AvailabilitySlotDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.AvailabilitySlotRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.AvailabilitySlotUpdateDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface AvailabilityMapper {

    default AvailabilitySlotDto toDto(AvailabilitySlot entity) {
        Availability avSample = entity.getMembers().getFirst();
        return AvailabilitySlotDto.builder()
                .id(entity.getId())
                .reservableId(entity.getReservable().getId())
                .startTime(avSample.getStartTime())
                .endTime(avSample.getEndTime())
                .dateFrom(avSample.getDateFrom())
                .dateTo(avSample.getDateTo())
                .build();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "reservable", ignore = true)
    default AvailabilitySlot toEntity(AvailabilitySlotRegisterDto dto){
        List<Availability> members = dtoToMembers(
                dto.daysOfWeek(),
                dto.dateFrom(),
                dto.dateTo(),
                dto.startTime(),
                dto.endTime()
        );
        return AvailabilitySlot.builder()
                .members(members)
                .build();
    }


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "reservable", ignore = true)
    default AvailabilitySlot updateEntity(@MappingTarget AvailabilitySlot entity, AvailabilitySlotUpdateDto dto){
        List<Availability> members = dtoToMembers(
                dto.daysOfWeek(),
                dto.dateFrom(),
                dto.dateTo(),
                dto.startTime(),
                dto.endTime()
        );
        entity.setMembers(members);
        return entity;
    }

    private List<Availability> dtoToMembers(Set<DayOfWeek> daysOfWeek, LocalDate dateFrom, LocalDate dateTo, LocalTime startTime, LocalTime endTime) {
        List<Availability> members = new ArrayList<>();
        for(DayOfWeek dow: daysOfWeek){
            Availability availability = Availability.builder()
                    .dayOfWeek(dow)
                    .dateFrom(dateFrom)
                    .dateTo(dateTo)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();
            members.add(availability);
        }
        return members;
    }
}
