package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.reservables.domain.model.Availability;
import dev.spiffocode.sigesapi.reservables.domain.model.AvailabilityException;
import dev.spiffocode.sigesapi.reservables.domain.model.AvailabilitySlot;
import dev.spiffocode.sigesapi.reservables.presentation.dto.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AvailabilityMapper {

    default AvailabilitySlotDto toDto(AvailabilitySlot entity) {
        Availability avSample = entity.getMembers().getFirst();
        Set<DayOfWeek> dayOfWeeks = entity.getMembers().stream()
                .map(Availability::getDayOfWeek)
                .collect(Collectors.toSet());
        return AvailabilitySlotDto.builder()
                .id(entity.getId())
                .reservableId(entity.getReservable().getId())
                .startTime(avSample.getStartTime())
                .endTime(avSample.getEndTime())
                .dateFrom(avSample.getDateFrom())
                .dateTo(avSample.getDateTo())
                .daysOfWeek(dayOfWeeks)
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
        AvailabilitySlot entity = AvailabilitySlot.builder()
                .members(members)
                .build();

        linkRelation(entity);
        return entity;
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
        linkRelation(entity);
        return entity;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "reservable", ignore = true)
    AvailabilityException toEntity(AvailabilityExceptionRegisterDto dto);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "reservable", ignore = true)
    AvailabilityException updateEntity(@MappingTarget AvailabilityException entity,  AvailabilityExceptionRegisterDto dto);

    AvailabilityExceptionDto toDto(AvailabilityException entity);


    @AfterMapping
    default void linkRelation(@MappingTarget AvailabilitySlot entity){
        if(entity.getMembers() == null || entity.getMembers().isEmpty()){
            return;
        }

        entity.getMembers().forEach(av -> av.setGroup(entity));
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
