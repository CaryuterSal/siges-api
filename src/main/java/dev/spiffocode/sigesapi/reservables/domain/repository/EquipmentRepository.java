package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
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
public interface EquipmentRepository extends
        JpaRepository<@NonNull Equipment, @NonNull Long>,
        RevisionRepository<@NonNull Equipment, @NonNull Long, @NonNull Long>,
        JpaSpecificationExecutor<@NonNull Equipment> {


    @Query(value = """
    SELECT r.*, e.* FROM equipments e
    INNER JOIN reservables r ON r.id = e.id
    INNER JOIN inventory_items i ON i.inventory_num = e.inventory_item_id
    WHERE r.deleted_at IS NOT NULL
    """, nativeQuery = true)
    List<Equipment> findAllDeleted();


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE reservables
        SET deleted_at = NOW()
        FROM equipments e
        WHERE reservables.id = e.id AND reservables.id = :id;
    
        UPDATE inventory_items
        SET deleted_at = NOW()
        FROM equipments e
        WHERE inventory_items.inventory_num = e.inventory_item_id AND e.id = :id;
    """, nativeQuery = true)
    int softDeleteById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE reservables
        SET deleted_at = NULL
        FROM equipments e
        WHERE reservables.id = e.id AND reservables.id = :id;
    
        UPDATE inventory_items
        SET deleted_at = NULL
        FROM equipments e
        WHERE inventory_items.inventory_num = e.inventory_item_id AND e.id = :id;
    """, nativeQuery = true)
    int restore(@Param("id") Long id);

    List<Equipment> findBySpaceId(Long spaceId);
    boolean existsBySpaceId(Long spaceId);
    boolean existsByTypeId(Long id);
}
