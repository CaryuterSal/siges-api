package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceTypeRepository extends JpaRepository<@NonNull SpaceType, @NonNull Long> {

    @Query(value = "SELECT * from space_types WHERE deleted_at IS NOT NULL", nativeQuery = true)
    List<Building> findAllDeleted();
}
