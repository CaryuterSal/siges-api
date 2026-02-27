package dev.spiffocode.sigesapi.reservables.domain.specification;

import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

public class SpaceTypeSpecifications {

    public static Specification<@NonNull SpaceType> onlyDeleted(){
        return (root, query, cb) -> cb.isNotNull(root.get("deletedAt"));
    }

    public static Specification<@NonNull SpaceType> onlyActive(){
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<@NonNull SpaceType> byActiveFilter(ShowModeFilter filter) {
        return switch (filter) {
            case ACTIVE -> onlyActive();
            case INACTIVE -> onlyDeleted();
            case ALL -> (root, query, cb) -> null;
        };
    }

}
