package dev.spiffocode.sigesapi.notifications.application.mapper;

import dev.spiffocode.sigesapi.notifications.domain.model.Notification;
import dev.spiffocode.sigesapi.notifications.presentation.NotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ReservationSummaryMapper.class})
public interface NotificationMapper {

    @Mapping(source = "relatedReservation", target = "reservation")
    @Mapping(source = "body", target = "message")
    NotificationResponse toDto(Notification notification);
}
