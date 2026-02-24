package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository
        extends JpaRepository<@NonNull Equipment, @NonNull Long>, JpaSpecificationExecutor<@NonNull Equipment> {


    @Query(value = """
    SELECT r.* FROM equipments e
    INNER JOIN reservables r ON r.id = e.id
    WHERE r.deleted_at IS NOT NULL
    """, nativeQuery = true)
    List<Equipment> findAllDeleted();

    List<Equipment> findBySpaceId(Long spaceId);
}
