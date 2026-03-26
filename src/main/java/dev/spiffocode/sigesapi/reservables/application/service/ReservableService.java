package dev.spiffocode.sigesapi.reservables.application.service;

import dev.spiffocode.sigesapi.reservables.presentation.dto.ReservableDto;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservableService {
    @NonNull
    ReservableDto getReservableById(long id);

    @NonNull
    Page<@NonNull ReservableDto> searchReservablesByFilter(Pageable pageable, ReservableFilter filter);
}
