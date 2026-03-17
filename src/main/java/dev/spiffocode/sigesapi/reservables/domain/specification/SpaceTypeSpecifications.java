package dev.spiffocode.sigesapi.reservables.domain.specification;

import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceTypeFilter;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

public class SpaceTypeSpecifications {

    public static Specification<@NonNull SpaceType> byFilter(SpaceTypeFilter filter){
        return Specification
                .where(byActiveFilter(filter.showModeFilter()))
                .and(
                        nameContains(filter.query())
                                .or(descriptionContains(filter.query()))
                );
    }

    public static Specification<@NonNull SpaceType> nameContains(String query){
        return (root, q, cb) ->
                query == null || query.isBlank() ? null : cb.like(cb.lower(root.get("name")), "%"+query.toLowerCase()+"%");
    }

    public static Specification<@NonNull SpaceType> descriptionContains(String query){
        return (root, q, cb) ->
                query == null || query.isBlank() ? null : cb.like(cb.lower(root.get("description")), "%"+query.toLowerCase()+"%");
    }


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
