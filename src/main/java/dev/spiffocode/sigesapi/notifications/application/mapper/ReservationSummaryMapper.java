package dev.spiffocode.sigesapi.notifications.application.mapper;

import dev.spiffocode.sigesapi.notifications.presentation.ReservationSummaryResponse;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservationSummaryMapper {

    ReservationSummaryResponse toDto(Reservation reservation);
}
