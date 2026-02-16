package dev.spiffocode.sigesapi.reservables.repository;

import dev.spiffocode.sigesapi.reservables.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.model.Space;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceRepository extends JpaRepository<Space, Long> {

    List<Space> findByTypeId(Long spaceTypeId);

    List<Space> findByBuildingId(Long buildingId);

    List<Space> findByBuildingIdAndStatus(Long buildingId, ReservableStatus status);
}
