package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservableRepository extends JpaRepository<Reservable, Long> {

    List<Reservable> findByBuildingId(Long buildingId);

    List<Reservable> findByStatus(ReservableStatus status);
}
