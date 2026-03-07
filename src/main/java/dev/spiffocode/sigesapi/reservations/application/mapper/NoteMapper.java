package dev.spiffocode.sigesapi.reservations.application.mapper;

import dev.spiffocode.sigesapi.reservations.domain.model.Note;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.presentation.EditNoteRequest;
import dev.spiffocode.sigesapi.reservations.presentation.NoteItem;
import dev.spiffocode.sigesapi.reservations.presentation.PublishNoteRequest;
import dev.spiffocode.sigesapi.users.application.mapper.UserMapper;
import dev.spiffocode.sigesapi.users.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface NoteMapper {

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservation", source = "reservation")
    Note toEntity(PublishNoteRequest noteItem, Reservation reservation);


    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reservation", ignore = true)
    Note updateEntity(@MappingTarget Note entity, EditNoteRequest noteItem);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", source = "author")
    NoteItem toDto(Note note, User author);
}
