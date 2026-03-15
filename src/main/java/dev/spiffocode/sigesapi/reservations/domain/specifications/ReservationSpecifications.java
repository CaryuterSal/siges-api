package dev.spiffocode.sigesapi.reservations.domain.specifications;

import dev.spiffocode.sigesapi.reservations.domain.model.GroupingType;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.infrastructure.service.impl.ReservationFilterRequest;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ReservationSpecifications {
    public static Specification<@NonNull Reservation> specificationFromFilter(ReservationFilterRequest filter, Long userId, boolean isApplicant) {
        return Specification
                .where(byPetitionerId(filter.petitionerId()))
                .and(byPetitionerName(filter.petitionerName()))
                .and(byDate(filter.date()))
                .and(byDateFrom(filter.dateFrom()))
                .and(byDateTo(filter.dateTo()))
                .and(byStatus(filter.status()))
                .and(byReservableId(filter.reservableId()))
                .and(byType(filter.type()))
                .and(filterOutIfApplicant(userId, isApplicant));
    }

    private static Specification<@NonNull Reservation> filterOutIfApplicant(Long userId, boolean isApplicant) {
        return (root, query, cb) -> isApplicant ?
                cb.equal(root.get("petitioner").get("id"), userId) : null;
    }

    private static Specification<@NonNull Reservation> byPetitionerId(Long id) {
        return (root, query, cb) -> id == null ? null
                : cb.equal(root.get("petitioner").get("id"), id);
    }

    private static Specification<@NonNull Reservation> byPetitionerName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            String pattern = "%" + name.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("petitioner").get("firstName")), pattern),
                    cb.like(cb.lower(root.get("petitioner").get("lastName")), pattern)
            );
        };
    }

    private static Specification<@NonNull Reservation> byDate(LocalDate date) {
        return (root, query, cb) -> date == null ? null
                : cb.equal(root.get("date"), date);
    }

    private static Specification<@NonNull Reservation> byDateFrom(LocalDate from) {
        return (root, query, cb) -> from == null ? null
                : cb.greaterThanOrEqualTo(root.get("date"), from);
    }

    private static Specification<@NonNull Reservation> byDateTo(LocalDate to) {
        return (root, query, cb) -> to == null ? null
                : cb.lessThanOrEqualTo(root.get("date"), to);
    }

    private static Specification<@NonNull Reservation> byStatus(Status status) {
        return (root, query, cb) -> status == null ? null
                : cb.equal(root.get("status"), status);
    }

    private static Specification<@NonNull Reservation> byReservableId(Long id) {
        return (root, query, cb) -> id == null ? null
                : cb.equal(root.get("reservable").get("id"), id);
    }

    private static Specification<@NonNull Reservation> byType(GroupingType type) {
        return (root, query, cb) -> type == null ? null
                : cb.equal(root.get("type"), type);
    }
}
