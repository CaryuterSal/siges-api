package dev.spiffocode.sigesapi.reservables.domain.specification;

import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

public class BuildingSpecifications {

    public static Specification<@NonNull Building> onlyDeleted(){
        return (root, query, cb) -> cb.isNotNull(root.get("deletedAt"));
    }

    public static Specification<@NonNull Building> onlyActive(){
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<@NonNull Building> byActiveFilter(ShowModeFilter filter) {
        return switch (filter) {
            case ACTIVE -> onlyActive();
            case INACTIVE -> onlyDeleted();
            case ALL -> (root, query, cb) -> null;
        };
    }
}
