package dev.spiffocode.sigesapi.reservables.domain.specification;

import dev.spiffocode.sigesapi.reservables.application.service.SpaceFilter;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

import java.time.Duration;

import static dev.spiffocode.sigesapi.common.domain.specification.SpecificationHelper.cast;
import static dev.spiffocode.sigesapi.reservables.domain.specification.ReservableSpecifications.*;
import static dev.spiffocode.sigesapi.reservables.domain.specification.ReservableSpecifications.availableForStudents;

public class SpaceSpecifications {

    public static Specification<@NonNull Space> spaceSpecification(SpaceFilter filter){
       return Specification
                .where(isOfType(filter.spaceTypeIdFilter()))
                .and(hasCapacityAtLeast(filter.capacityAtLeastFilter()))
                .and(cast(isAvailableBySchedule(filter.requestStartFilter(), filter.requestEndFilter())))
                .and(cast(hasNoExceptionFor(filter.requestStartFilter(), filter.requestEndFilter())))
                .and(cast(statusIs(filter.statusFilter())))
                .and(
                        cast(descriptionContains(filter.searchQuery())
                                .or(nameContains(filter.searchQuery())))
                )
                .and(cast(inBuilding(filter.buildingIdFilter())))
                .and(cast(availableForStudents(filter.studentsAvailableFilter())))
                .and(cast(byActiveFilter(filter.showModeFilter())));
    }

    public static Specification<@NonNull Space> isOfType(Long spaceTypeId){
        return (root, query, cb) ->
                spaceTypeId == null ? null : cb.equal(root.get("type").get("id"), spaceTypeId);
    }

    public static Specification<@NonNull Space> hasCapacity(Integer capacity){
        return (root, query, cb) ->
                capacity == null ? null : cb.equal(root.get("capacity"), capacity);
    }

    public static Specification<@NonNull Space> hasCapacityAtLeast(Integer capacity){
        return (root, query, cb) ->
                capacity == null ? null : cb.greaterThanOrEqualTo(root.get("capacity"), capacity);
    }

    public static Specification<@NonNull Space> hasCapacityAtMost(Integer capacity){
        return (root, query, cb) ->
                capacity == null ? null : cb.lessThanOrEqualTo(root.get("capacity"), capacity);
    }

    public static Specification<@NonNull Space> needsToBeBookedAtLeast(Duration inAdvance){
        return (root, query, cb) ->
                inAdvance == null ? null : cb.greaterThanOrEqualTo(root.get("bookInAdvance").as(Duration.class), inAdvance);
    }

    public static Specification<@NonNull Space> needsToBeBookedAtMost(Duration inAdvance){
        return (root, query, cb) ->
                inAdvance == null ? null : cb.lessThanOrEqualTo(root.get("bookInAdvance").as(Duration.class), inAdvance);
    }
}
