package dev.spiffocode.sigesapi.reservables.domain.specification;

import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

public class ReservableSpecifications {

    public static Specification<@NonNull Reservable> descriptionContains(String description){
        return (root, query, cb) -> {
            if(description == null || description.isEmpty()){
                return null;
            }
            return cb.like(cb.lower(root.get("description").as(String.class)), "%"+description.toLowerCase()+"%");
        };
    }

    public static Specification<@NonNull Reservable> statusIs(ReservableStatus status){
        return (root, query, cb) -> {
            return status == null ? null : cb.equal(root.get("status"), status);
        };
    }

    public static Specification<@NonNull Reservable> availableForStudents(boolean available){
        return (root, query, cb) -> {
            return cb.equal(root.get("studentsAvailable"), available);
        };
    }

    public static Specification<@NonNull Reservable> inBuilding(Long id){
        return (root, query, cb) -> {
            return id == null ? null : cb.equal(root.get("building").get("id"), id);
        };
    }
}
