package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpaceTypeRepository extends JpaRepository<@NonNull SpaceType, @NonNull Long> {

    @Query(value = "SELECT * from space_types WHERE deleted_at IS NOT NULL", nativeQuery = true)
    List<SpaceType> findAllDeleted();


    @Modifying
    @Query(value = "UPDATE space_types SET deleted_at = NULL WHERE id = :id", nativeQuery = true)
    int restore(@Param("id") Long id);
}
