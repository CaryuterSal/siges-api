package dev.spiffocode.sigesapi.reservables.domain.specification;

import dev.spiffocode.sigesapi.reservables.application.service.EquipmentTypeFilter;
import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.domain.model.EquipmentType;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

public class EquipmentTypeSpecifications {


    public static Specification<@NonNull EquipmentType> byFilter(EquipmentTypeFilter filter){
        return Specification
                .where(byActiveFilter(filter.showModeFilter()))
                .and(
                        nameContains(filter.query())
                                .or(descriptionContains(filter.query()))
                );
    }

    public static Specification<@NonNull EquipmentType> nameContains(String query){
        return (root, q, cb) ->
                query == null || query.isBlank() ? null : cb.like(cb.lower(root.get("name")), "%"+query.toLowerCase()+"%");
    }

    public static Specification<@NonNull EquipmentType> descriptionContains(String query){
        return (root, q, cb) ->
                query == null || query.isBlank() ? null : cb.like(cb.lower(root.get("description")), "%"+query.toLowerCase()+"%");
    }


    public static Specification<@NonNull EquipmentType> onlyDeleted() {
        return (root, query, cb) -> cb.isNotNull(root.get("deletedAt"));
    }

    public static Specification<@NonNull EquipmentType> onlyActive() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<@NonNull EquipmentType> byActiveFilter(ShowModeFilter filter) {
        return switch (filter) {
            case ACTIVE -> onlyActive();
            case INACTIVE -> onlyDeleted();
            case ALL -> (root, query, cb) -> null;
        };
    }

}
