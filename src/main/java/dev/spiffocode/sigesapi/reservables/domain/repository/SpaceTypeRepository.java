package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceTypeRepository extends JpaRepository<@NonNull SpaceType, @NonNull Long>, JpaSpecificationExecutor<@NonNull SpaceType> {

    @Query(value = "SELECT * from space_types WHERE deleted_at IS NOT NULL", nativeQuery = true)
    List<SpaceType> findAllDeleted();

    @Query(
            value = "SELECT * from buildings WHERE deleted_at IS NOT NULL",
            countQuery = "SELECT COUNT(*) FROM buildings WHERE deleted_at IS NOT NULL",
            nativeQuery = true)
    Page<@NonNull Building> findAllDeletedPaged(Pageable pageable);

    @Query(value = "SELECT * from buildings", nativeQuery = true)
    List<SpaceType> findAllActiveAndDeleted();

    @Modifying
    @Query(value = "UPDATE space_types SET deleted_at = NULL WHERE id = :id", nativeQuery = true)
    int restore(@Param("id") Long id);
}
