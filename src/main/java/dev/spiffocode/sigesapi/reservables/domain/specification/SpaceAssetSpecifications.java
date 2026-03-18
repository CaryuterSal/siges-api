package dev.spiffocode.sigesapi.reservables.domain.specification;

import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.application.service.SpaceAssetFilter;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceAsset;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

public class SpaceAssetSpecifications {

    public static Specification<@NonNull SpaceAsset> byFilter(SpaceAssetFilter filter) {
        return Specification
                .where(inSpace(filter.spaceIdFilter()))
                .and(
                        inventoryNumContains(filter.searchQuery())
                                .or(descriptionContains(filter.searchQuery()))
                                .or(nameContains(filter.searchQuery()))
                )
                .and(inBuilding(filter.buildingIdFilter()))
                .and(inType(filter.equipmentTypeIdFilter()))
                .and(byActiveFilter(filter.showModeFilter()));

    }

    public static Specification<@NonNull SpaceAsset> inSpace(Long id){
        return (root, query, cb) ->
                id == null ? null : cb.equal(root.get("space").get("id"), id);
    }

    public static Specification<@NonNull SpaceAsset> inventoryNumContains(String inventoryNum){
        return (root, query, cb) -> {
            if(inventoryNum == null || inventoryNum.isBlank()) return null;
            return cb.like(cb.lower(root.get("inventoryItem").get("inventoryNum")), "%" + inventoryNum.toLowerCase() + "%");
        };
    }

    public static Specification<@NonNull SpaceAsset> inType(Long id){
        return (root, query, cb) ->
                id == null ? null : cb.equal(root.get("type").get("id"), id);
    }


    public static Specification<@NonNull SpaceAsset> nameContains(String name){
        return (root, query, cb) -> {
            if(name == null || name.isEmpty()){
                return null;
            }
            return cb.like(cb.lower(root.get("name").as(String.class)), "%"+name.toLowerCase()+"%");
        };
    }


    public static Specification<@NonNull SpaceAsset> descriptionContains(String description){
        return (root, query, cb) -> {
            if(description == null || description.isEmpty()){
                return null;
            }
            return cb.like(cb.lower(root.get("description").as(String.class)), "%"+description.toLowerCase()+"%");
        };
    }

    public static Specification<@NonNull SpaceAsset> inBuilding(Long id){
        return (root, query, cb) -> {
            return id == null ? null : cb.equal(root.get("space").get("building").get("id"), id);
        };
    }


    public static Specification<@NonNull SpaceAsset> onlyDeleted(){
        return (root, query, cb) -> cb.isNotNull(root.get("deletedAt"));
    }

    public static Specification<@NonNull SpaceAsset> onlyActive(){
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<@NonNull SpaceAsset> byActiveFilter(ShowModeFilter filter) {
        return switch (filter) {
            case ACTIVE -> onlyActive();
            case INACTIVE -> onlyDeleted();
            case ALL -> (root, query, cb) -> null;
        };
    }

}
