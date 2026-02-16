package dev.spiffocode.sigesapi.reservables.repository;

import dev.spiffocode.sigesapi.reservables.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findBySpaceId(Long spaceId);
}
