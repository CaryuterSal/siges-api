package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.InventoryItem;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepository extends JpaRepository<@NonNull InventoryItem,@NonNull Long> {
    boolean existsByInventoryNum(String inventoryNum);
    boolean existsByInventoryNumAndIdNot(String inventoryNum, Long id);
}
