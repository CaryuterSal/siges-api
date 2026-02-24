package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.AvailabilityException;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvailabilityExceptionRepository extends JpaRepository<@NonNull AvailabilityException, @NonNull Long> {

    List<AvailabilityException> findByReservableId(Long reservableId);
}
