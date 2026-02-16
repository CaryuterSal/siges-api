package dev.spiffocode.sigesapi.reservables.repository;

import dev.spiffocode.sigesapi.reservables.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {

    List<Availability> findByReservableId(Long reservableId);

    List<Availability> findByDayOfWeek(DayOfWeek dayOfWeek);
}
