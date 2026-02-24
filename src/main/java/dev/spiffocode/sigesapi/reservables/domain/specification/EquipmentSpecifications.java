package dev.spiffocode.sigesapi.reservables.domain.specification;

import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

public class EquipmentSpecifications {

    public static Specification<@NonNull Equipment> inSpace(Long id){
        return (root, query, cb) ->
                id == null ? null : cb.equal(root.get("space").get("id"), id);
    }

    public static Specification<@NonNull Equipment> inventoryNumContains(String inventoryNum){
        return (root, query, cb) -> {
            if(inventoryNum == null || inventoryNum.isBlank()) return null;
            return cb.like(cb.lower(root.get("inventoryNum")), "%" + inventoryNum.toLowerCase() + "%");
        };
    }
}
