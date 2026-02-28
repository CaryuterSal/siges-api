package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceRepository extends
        JpaRepository<@NonNull Space, @NonNull Long>,
        RevisionRepository<@NonNull Space, @NonNull Long, @NonNull Long>,
        JpaSpecificationExecutor<@NonNull Space> {

    @Query(value = """
    SELECT r.*, s.* FROM spaces s
    INNER JOIN reservables r ON r.id = s.id
    WHERE r.deleted_at IS NOT NULL
    """, nativeQuery = true)
    List<Space> findAllDeleted();


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE reservables
        SET deleted_at = NOW()
        FROM spaces s
        WHERE reservables.id = s.id AND reservables.id = :id
    """, nativeQuery = true)
    int softDeleteById(@Param("id") Long id);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE reservables
        SET deleted_at = NOW()
        FROM spaces s
        WHERE reservables.id = s.id AND reservables.id = :id
    """, nativeQuery = true)
    int restore(@Param("id") Long id);

    List<Space> findByTypeId(Long spaceTypeId);

    List<Space> findByBuildingId(Long buildingId);

    List<Space> findByBuildingIdAndStatus(Long buildingId, ReservableStatus status);


    boolean existsByNameAndBuilding(String name, Building building);
    boolean existsByNameAndBuildingAndIdNot(String name, Building building, Long id);
}
