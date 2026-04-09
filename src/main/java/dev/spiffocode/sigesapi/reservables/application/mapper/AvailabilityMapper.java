package dev.spiffocode.sigesapi.reservables.application.mapper;

import dev.spiffocode.sigesapi.reservables.domain.model.*;
import dev.spiffocode.sigesapi.reservables.presentation.dto.*;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AvailabilityMapper {

    default AvailabilitySlotDto toDto(AvailabilitySlot entity) {
        if (entity == null || entity.getMembers() == null || entity.getMembers().isEmpty()) {
            return null;
        }
        Availability avSample = entity.getMembers().getFirst();
        Set<DayOfWeek> dayOfWeeks = entity.getMembers().stream()
                .map(Availability::getDayOfWeek)
                .collect(Collectors.toSet());
        return AvailabilitySlotDto.builder()
                .id(entity.getId())
                .reservableId(entity.getReservable() != null ? entity.getReservable().getId() : null)
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
    default AvailabilitySlot toEntity(AvailabilitySlotRegisterDto dto) {
        List<Availability> members = dtoToMembers(
                dto.daysOfWeek(),
                dto.dateFrom(),
                dto.dateTo(),
                dto.startTime(),
                dto.endTime());

        AvailabilitySlot entity = new AvailabilitySlot();
        for (Availability member : members) {
            entity.addMember(member);
        }
        return entity;
    }

    @Mapping(target = "id", source = "dto.id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "reservable", ignore = true)
    default AvailabilitySlot toEntity(AvailabilitySlotUpdateDto dto) {
        List<Availability> members = dtoToMembers(
                dto.daysOfWeek(),
                dto.dateFrom(),
                dto.dateTo(),
                dto.startTime(),
                dto.endTime());

        AvailabilitySlot entity = new AvailabilitySlot();
        entity.setId(dto.id());
        for (Availability member : members) {
            entity.addMember(member);
        }
        return entity;
    }

    default void updateAvailabilitySlots(List<AvailabilitySlotUpdateDto> dtos,
            @MappingTarget List<AvailabilitySlot> entities,
            Reservable reservable) {
        if (dtos == null) {
            return;
        }

        Map<Long, AvailabilitySlot> existingItems = entities.stream()
                .filter(it -> it.getId() != null)
                .collect(Collectors.toMap(AvailabilitySlot::getId, it -> it));

        List<AvailabilitySlot> currentNewItems = new ArrayList<>();
        Set<Long> updatedIds = new HashSet<>();

        for (AvailabilitySlotUpdateDto dto : dtos) {
            if (dto.id() != null && existingItems.containsKey(dto.id())) {
                AvailabilitySlot existing = existingItems.get(dto.id());
                updateEntity(existing, dto);
                updatedIds.add(dto.id());
            } else {
                AvailabilitySlot newSlot = toEntity(dto);
                newSlot.setReservable(reservable);
                currentNewItems.add(newSlot);
            }
        }
        entities.addAll(currentNewItems);
        entities.removeIf(it -> it.getId() != null && !updatedIds.contains(it.getId()));
    }

    default void updateEntity(@MappingTarget AvailabilitySlot entity, AvailabilitySlotUpdateDto dto) {
        List<Availability> newMembers = dtoToMembers(
                dto.daysOfWeek(),
                dto.dateFrom(),
                dto.dateTo(),
                dto.startTime(),
                dto.endTime());

        entity.getMembers().clear();
        for (Availability member : newMembers) {
            entity.addMember(member);
        }
    }

    default void updateAvailabilityExceptions(List<AvailabilityExceptionUpdateDto> dtos,
            @MappingTarget List<AvailabilityException> entities,
            Reservable reservable) {
        if (dtos == null) {
            return;
        }

        Map<Long, AvailabilityException> existingItems = entities.stream()
                .filter(it -> it.getId() != null)
                .collect(Collectors.toMap(AvailabilityException::getId, it -> it));

        List<AvailabilityException> currentNewItems = new ArrayList<>();
        Set<Long> updatedIds = new HashSet<>();

        for (AvailabilityExceptionUpdateDto dto : dtos) {
            if (dto.id() != null && existingItems.containsKey(dto.id())) {
                AvailabilityException existing = existingItems.get(dto.id());
                updateEntity(existing, dto);
                updatedIds.add(dto.id());
            } else {
                AvailabilityException newEx = toEntity(dto);
                newEx.setReservable(reservable);
                currentNewItems.add(newEx);
            }
        }

        entities.addAll(currentNewItems);
        entities.removeIf(it -> it.getId() != null && !updatedIds.contains(it.getId()));
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "reservable", ignore = true)
    AvailabilityException toEntity(AvailabilityExceptionUpdateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "reservable", ignore = true)
    void updateEntity(@MappingTarget AvailabilityException entity, AvailabilityExceptionUpdateDto dto);

    AvailabilityExceptionDto toDto(AvailabilityException entity);

    @AfterMapping
    default void linkRelation(@MappingTarget AvailabilitySlot entity) {
        if (entity.getMembers() == null || entity.getMembers().isEmpty()) {
            return;
        }

        entity.getMembers().forEach(av -> av.setGroup(entity));
    }

    private List<Availability> dtoToMembers(Set<DayOfWeek> daysOfWeek, LocalDate dateFrom, LocalDate dateTo,
            LocalTime startTime, LocalTime endTime) {
        List<Availability> members = new ArrayList<>();
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return members;
        }
        LocalDate effectiveDateFrom = dateFrom != null ? dateFrom : LocalDate.now();
        for (DayOfWeek dow : daysOfWeek) {
            Availability availability = Availability.builder()
                    .dayOfWeek(dow)
                    .dateFrom(effectiveDateFrom)
                    .dateTo(dateTo)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();
            members.add(availability);
        }
        return members;
    }
}
