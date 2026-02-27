package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.Availability;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<@NonNull Availability, @NonNull Long> {

    List<Availability> findByDayOfWeek(DayOfWeek dayOfWeek);
}
