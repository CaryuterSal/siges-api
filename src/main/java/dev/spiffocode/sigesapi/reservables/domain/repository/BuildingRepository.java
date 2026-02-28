package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuildingRepository extends
        JpaRepository<@NonNull Building, @NonNull Long>,
        RevisionRepository<@NonNull Building, @NonNull Long, @NonNull Long>,
        JpaSpecificationExecutor<@NonNull Building> {

    @Query(value = "SELECT * from buildings WHERE deleted_at IS NOT NULL", nativeQuery = true)
    List<Building> findAllDeleted();

    @Query(
            value = "SELECT * from buildings WHERE deleted_at IS NOT NULL",
            countQuery = "SELECT COUNT(*) FROM buildings WHERE deleted_at IS NOT NULL",
            nativeQuery = true)
    Page<@NonNull Building> findAllDeletedPaged(Pageable pageable);

    @Query(value = "SELECT * from buildings", nativeQuery = true)
    List<Building> findAllActiveAndDeleted();

    @Query(
            value = "SELECT * from buildings",
            countQuery = "SELECT COUNT(*) FROM buildings",
            nativeQuery = true)
    Page<@NonNull Building> findAllActiveAndDeletedPaged(Pageable pageable);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE buildings SET deleted_at = NOW() WHERE id = :id", nativeQuery = true)
    int softDeleteById(@Param("id") Long id);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE buildings SET deleted_at = NULL WHERE id = :id", nativeQuery = true)
    int restore(@Param("id") Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
