package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservableRepository extends JpaRepository<@NonNull Reservable, @NonNull Long> {

    List<Reservable> findByBuildingId(Long buildingId);

    List<Reservable> findByStatus(ReservableStatus status);
}
