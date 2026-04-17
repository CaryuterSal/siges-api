package dev.spiffocode.sigesapi.reservables.infrastructure.service.impl;

import dev.spiffocode.sigesapi.auth.infrastructure.SecurityContextHelper;
import dev.spiffocode.sigesapi.common.infrastructure.persistence.WithDeletedRecords;
import dev.spiffocode.sigesapi.reservables.application.mapper.ReservableMapper;
import dev.spiffocode.sigesapi.reservables.application.service.ReservableFilter;
import dev.spiffocode.sigesapi.reservables.application.service.ReservableService;
import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.domain.exception.ReservableNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservables.domain.repository.ReservableRepository;
import dev.spiffocode.sigesapi.reservables.domain.specification.ReservableSpecifications;
import dev.spiffocode.sigesapi.reservables.presentation.dto.ReservableDto;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservableServiceImpl implements ReservableService {

    private final ReservableRepository reservableRepository;
    private final ReservableMapper reservableMapper;
    private final SecurityContextHelper securityContextHelper;


    @Override
    @PostAuthorize("!hasRole('APPLICANT') or returnObject.deletedAt == null")
    @WithDeletedRecords
    @Transactional(readOnly = true)
    public @NonNull ReservableDto getReservableById(long id) {
        Reservable reservable = reservableRepository.findById(id)
                .orElseThrow(() -> new ReservableNotFoundException(id));
        return reservableMapper.toDto(reservable);
    }

    @Override
    @Transactional(readOnly = true)
    @WithDeletedRecords
    public @NonNull Page<@NonNull ReservableDto> searchReservablesByFilter(Pageable pageable, ReservableFilter filter) {

        return reservableRepository.findAll(resolveSpecification(filter), pageable)
                .map(reservableMapper::toDto);
    }

    private Specification<@NonNull Reservable> resolveSpecification(ReservableFilter filter) {
        ReservableFilter actualFilter = securityContextHelper.isAdmin() ? filter : filter.withShowModeFilter(ShowModeFilter.ACTIVE);
        Specification<@NonNull Reservable> spec = ReservableSpecifications.byFilter(actualFilter);

        if(securityContextHelper.isStudent()) return spec.and(ReservableSpecifications.availableForStudents(true));
        return spec;

    }
}
