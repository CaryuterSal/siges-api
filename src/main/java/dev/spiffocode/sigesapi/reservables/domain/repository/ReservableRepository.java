package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservableRepository extends JpaRepository<@NonNull Reservable, @NonNull Long> {


    @Query(value = "SELECT * from reservables WHERE deleted_at IS NOT NULL", nativeQuery = true)
    List<Building> findAllDeleted();

    List<Reservable> findByBuildingId(Long buildingId);

    List<Reservable> findByStatus(ReservableStatus status);
}
