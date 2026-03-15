package dev.spiffocode.sigesapi.reservations.application.mapper;

import dev.spiffocode.sigesapi.reservables.application.mapper.ReservableMapper;
import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.presentation.CreateReservationRequest;
import dev.spiffocode.sigesapi.reservations.presentation.NoteItem;
import dev.spiffocode.sigesapi.reservations.presentation.RescheduleReservationRequest;
import dev.spiffocode.sigesapi.reservations.presentation.ReservationResponse;
import dev.spiffocode.sigesapi.users.application.mapper.UserMapper;
import dev.spiffocode.sigesapi.users.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = { NoteMapper.class, UserMapper.class, ReservableMapper.class })
public interface ReservationMapper {

    @Mapping(target = "notes", source = "notes")
    ReservationResponse toDto(Reservation reservation, List<NoteItem> notes);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "petitioner", source = "petitioner")
    @Mapping(target = "reservable", source = "reservable")
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "finishedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    Reservation toEntity(CreateReservationRequest request, User petitioner, Reservable reservable);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "petitioner", ignore = true)
    @Mapping(target = "reservable", ignore = true)
    @Mapping(target = "companions", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "notes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "finishedAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    Reservation updateSchedule(@MappingTarget Reservation entity, RescheduleReservationRequest request);
}
