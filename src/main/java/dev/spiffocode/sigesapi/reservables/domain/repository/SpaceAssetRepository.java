package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.SpaceAsset;
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
public interface SpaceAssetRepository extends
        JpaRepository<@NonNull SpaceAsset, @NonNull Long>,
        RevisionRepository<@NonNull SpaceAsset, @NonNull Long, @NonNull Long>,
        JpaSpecificationExecutor<@NonNull SpaceAsset> {

    @Query(value = """
        SELECT sa.*, i.*
        FROM space_assets sa
        INNER JOIN inventory_items i ON i.id = sa.inventory_item_id
        WHERE sa.deleted_at IS NOT NULL
        """, nativeQuery = true)
    List<SpaceAsset> findAllDeleted();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE space_assets
        SET deleted_at = NOW()
        WHERE id = :id;
        
        
        UPDATE inventory_items
        SET deleted_at = NOW()
        FROM space_assets sa
        WHERE inventory_items.id = sa.inventory_item_id AND sa.id = :id;
        """, nativeQuery = true)
    int softDeleteById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE space_assets
        SET deleted_at = NULL
        WHERE id = :id;
        
        UPDATE inventory_items
        SET deleted_at = NULL
        FROM space_assets sa
        WHERE inventory_items.id = sa.inventory_item_id AND sa.id = :id
        """, nativeQuery = true)
    int restore(@Param("id") Long id);
}