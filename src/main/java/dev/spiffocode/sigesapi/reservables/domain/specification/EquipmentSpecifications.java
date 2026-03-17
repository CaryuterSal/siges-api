package dev.spiffocode.sigesapi.reservables.domain.specification;

import dev.spiffocode.sigesapi.reservables.application.service.EquipmentFilter;
import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

import static dev.spiffocode.sigesapi.common.domain.specification.SpecificationHelper.cast;
import static dev.spiffocode.sigesapi.reservables.domain.specification.ReservableSpecifications.*;

public class EquipmentSpecifications {

    public static Specification<@NonNull Equipment> equipmentSpecification(EquipmentFilter filter) {
        return Specification
                .where(inSpace(filter.spaceIdFilter()))
                .and(cast(isAvailableBySchedule(filter.requestStartFilter(), filter.requestEndFilter())))
                .and(cast(hasNoExceptionFor(filter.requestStartFilter(), filter.requestEndFilter())))
                .and(cast(statusIs(filter.statusFilter())))
                .and(
                        inventoryNumContains(filter.searchQuery())
                                .or(cast(descriptionContains(filter.searchQuery())))
                                .or(cast(nameContains(filter.searchQuery())))
                )
                .and(cast(inBuilding(filter.buildingIdFilter())))
                .and(inType(filter.equipmentTypeIdFilter()))
                .and(cast(availableForStudents(filter.studentsAvailableFilter())))
                .and(cast(byActiveFilter(filter.showModeFilter())));

    }

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

    public static Specification<@NonNull Equipment> inType(Long id){
        return (root, query, cb) ->
                id == null ? null : cb.equal(root.get("type").get("id"), id);
    }
}
