package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceRepository
        extends JpaRepository<@NonNull Space, @NonNull Long>, JpaSpecificationExecutor<@NonNull Space> {

    List<Space> findByTypeId(Long spaceTypeId);

    List<Space> findByBuildingId(Long buildingId);

    List<Space> findByBuildingIdAndStatus(Long buildingId, ReservableStatus status);
}
