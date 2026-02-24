package dev.spiffocode.sigesapi.reservables.domain.specification;

import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

import java.time.Duration;

public class SpaceSpecifications {

    public static Specification<@NonNull SpaceSpecifications> isOfType(SpaceType spaceType){
        return (root, query, cb) ->
                spaceType == null ? null : cb.equal(root.get("type"), spaceType);
    }

    public static Specification<@NonNull SpaceSpecifications> hasCapacity(Integer capacity){
        return (root, query, cb) ->
                capacity == null ? null : cb.equal(root.get("capacity"), capacity);
    }

    public static Specification<@NonNull SpaceSpecifications> hasCapacityAtLeast(Integer capacity){
        return (root, query, cb) ->
                capacity == null ? null : cb.greaterThanOrEqualTo(root.get("capacity"), capacity);
    }

    public static Specification<@NonNull SpaceSpecifications> hasCapacityAtMost(Integer capacity){
        return (root, query, cb) ->
                capacity == null ? null : cb.lessThanOrEqualTo(root.get("capacity"), capacity);
    }

    public static Specification<@NonNull SpaceSpecifications> needsToBeBookedAtLeast(Duration inAdvance){
        return (root, query, cb) ->
                inAdvance == null ? null : cb.greaterThanOrEqualTo(root.get("bookInAdvance").as(Duration.class), inAdvance);
    }

    public static Specification<@NonNull SpaceSpecifications> needsToBeBookedAtMost(Duration inAdvance){
        return (root, query, cb) ->
                inAdvance == null ? null : cb.lessThanOrEqualTo(root.get("bookInAdvance").as(Duration.class), inAdvance);
    }
}
