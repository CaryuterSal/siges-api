package dev.spiffocode.sigesapi.reservables.domain.specification;

import dev.spiffocode.sigesapi.reservables.application.service.ShowModeFilter;
import dev.spiffocode.sigesapi.reservables.domain.model.*;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReservableSpecifications {


    public static Specification<@NonNull Reservable> onlyDeleted(){
        return (root, query, cb) -> cb.isNotNull(root.get("deletedAt"));
    }

    public static Specification<@NonNull Reservable> onlyActive(){
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<@NonNull Reservable> byActiveFilter(ShowModeFilter filter) {
        return switch (filter) {
            case ACTIVE -> onlyActive();
            case INACTIVE -> onlyDeleted();
            case ALL -> (root, query, cb) -> null;
        };
    }

    public static Specification<@NonNull Reservable> nameContains(String name){
        return (root, query, cb) -> {
            if(name == null || name.isEmpty()){
                return null;
            }
            return cb.like(cb.lower(root.get("name").as(String.class)), "%"+name.toLowerCase()+"%");
        };
    }

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

    public static Specification<@NonNull Reservable> availableForStudents(Boolean available){
        return (root, query, cb) -> {
            return available == null ? null : cb.equal(root.get("studentsAvailable"), available);
        };
    }

    public static Specification<@NonNull Reservable> inBuilding(Long id){
        return (root, query, cb) -> {
            return id == null ? null : cb.equal(root.get("building").get("id"), id);
        };
    }


    /**
     * El recurso tiene al menos una Availability que cubre el rango pedido.
     * Condiciones:
     * - dayOfWeek coincide con el día de requestStart
     * - startTime <= requestStart.toLocalTime()
     * - endTime >= requestEnd.toLocalTime()
     * - dateFrom <= requestStart.toLocalDate()
     * - dateTo es null (sin vencimiento) o dateTo >= requestEnd.toLocalDate()
     */
    public static Specification<@NonNull Reservable> isAvailableBySchedule(
            LocalDateTime requestStart,
            LocalDateTime requestEnd
    ) {
        return (root, query, cb) -> {
            if(requestStart == null || requestEnd == null) return null;
            query.distinct(true);
            Join<Reservable, AvailabilitySlot> slot = root.join("availability");
            Join<AvailabilitySlot, Availability> av = slot.join("members");

            LocalDate date = requestStart.toLocalDate();
            LocalTime start = requestStart.toLocalTime();
            LocalTime end = requestEnd.toLocalTime();
            DayOfWeek dow = date.getDayOfWeek();

            return cb.and(
                    cb.equal(av.get("dayOfWeek"), dow),
                    cb.lessThanOrEqualTo(av.get("startTime"), start),
                    cb.greaterThanOrEqualTo(av.get("endTime"), end),
                    cb.lessThanOrEqualTo(av.get("dateFrom"), date),
                    cb.or(
                            cb.isNull(av.get("dateTo")),
                            cb.greaterThanOrEqualTo(av.get("dateTo"), date)
                    )
            );
        };
    }

    /**
     * El recurso NO tiene ninguna AvailabilityException que se solape con el rango.
     * Usa subquery EXISTS para verificar si hay excepción que lo cubra,
     * y luego niega con NOT EXISTS.
     */
    public static Specification<@NonNull Reservable> hasNoExceptionFor(
            LocalDateTime requestStart,
            LocalDateTime requestEnd
    ) {
        return (root, query, cb) -> {
            if(requestStart == null || requestEnd == null) return null;
            LocalDate reqDate = requestStart.toLocalDate();
            LocalTime reqStart = requestStart.toLocalTime();
            LocalTime reqEnd = requestEnd.toLocalTime();

            Subquery<Long> sub = query.subquery(Long.class);
            Root<AvailabilityException> ex = sub.from(AvailabilityException.class);
            sub.select(cb.literal(1L));

            sub.where(cb.and(
                    cb.equal(ex.get("reservable"), root),
                    cb.lessThanOrEqualTo(ex.get("dateFrom"), reqDate),
                    cb.greaterThanOrEqualTo(ex.get("dateTo"), reqDate),
                    cb.lessThan(ex.get("startTime"), reqEnd),
                    cb.greaterThan(ex.get("endTime"), reqStart)
            ));

            return cb.not(cb.exists(sub));
        };
    }
}
