package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.AvailabilityException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvailabilityExceptionRepository extends JpaRepository<AvailabilityException, Long> {

    List<AvailabilityException> findByReservableId(Long reservableId);
}
