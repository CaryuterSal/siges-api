package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.EquipmentType;
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
public interface EquipmentTypeRepository extends
        JpaRepository<@NonNull EquipmentType, @NonNull Long>,
        RevisionRepository<@NonNull EquipmentType, @NonNull Long, @NonNull Long>,
        JpaSpecificationExecutor<@NonNull EquipmentType> {

    @Query(value = "SELECT * from equipment_types WHERE deleted_at IS NOT NULL", nativeQuery = true)
    List<EquipmentType> findAllDeleted();

    @Query(value = "SELECT * from equipment_types WHERE deleted_at IS NOT NULL", countQuery = "SELECT COUNT(*) FROM equipment_types WHERE deleted_at IS NOT NULL", nativeQuery = true)
    Page<@NonNull EquipmentType> findAllDeletedPaged(Pageable pageable);

    @Query(value = "SELECT * from equipment_types", nativeQuery = true)
    List<EquipmentType> findAllActiveAndDeleted();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE equipment_types SET deleted_at = NOW() WHERE id = :id", nativeQuery = true)
    int softDeleteById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE equipment_types SET deleted_at = NULL WHERE id = :id", nativeQuery = true)
    int restore(@Param("id") Long id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
