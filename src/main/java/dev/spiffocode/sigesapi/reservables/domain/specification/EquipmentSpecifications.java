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
                .and(searchContains(filter.searchQuery()))
                .and(cast(inBuilding(filter.buildingIdFilter())))
                .and(inType(filter.equipmentTypeIdFilter()))
                .and(cast(availableForStudents(filter.studentsAvailableFilter())))
                .and(cast(byActiveFilter(filter.showModeFilter())));

    }

    public static Specification<@NonNull Equipment> inSpace(Long id) {
        return (root, query, cb) ->
                id == null ? null : cb.equal(root.get("space").get("id"), id);
    }

    public static Specification<@NonNull Equipment> inType(Long id) {
        return (root, query, cb) ->
                id == null ? null : cb.equal(root.get("type").get("id"), id);
    }

    public static Specification<@NonNull Equipment> searchContains(String searchQuery) {
        return (root, query, cb) -> {
            if (searchQuery == null || searchQuery.isBlank()) return null;
            String pattern = "%" + searchQuery.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("inventoryItem").get("inventoryNum")), pattern)
            );
        };
    }
}
