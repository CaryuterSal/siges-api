package dev.spiffocode.sigesapi.reservables.domain.model;

import dev.spiffocode.sigesapi.reservations.domain.exception.ReservationTooSoonException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.envers.Audited;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Audited
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@Table(name = "spaces")
@PrimaryKeyJoinColumn(name = "id")
public class Space extends Reservable {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_types_id", nullable = false)
    private SpaceType type;

    @NotNull
    private Duration bookInAdvance;

    @NotNull
    @Positive
    private Integer capacity;


    @Builder.Default
    @ToString.Exclude
    @Filter(name = "softDeleteFilter")
    @OneToMany(
            mappedBy = "space",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST}
    )
    private List<SpaceAsset> assets = new ArrayList<>();

    @Builder.Default
    @ToString.Exclude
    @Filter(name = "softDeleteFilter")
    @OneToMany(
            mappedBy = "space",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST}
    )
    private List<Equipment> equipments = new ArrayList<>();

    @Override
    protected void assertSpecificCanDoReservation(LocalDate requestedDate, LocalTime startTime, LocalTime endTime, Clock clock) {
        LocalDateTime requestedDateTime = LocalDateTime.of(requestedDate, startTime);

        Duration bookInAdvance = getBookInAdvance();
        LocalDateTime minimumAllowedDateTime = LocalDateTime.now(clock).plus(bookInAdvance);

        if (requestedDateTime.isBefore(minimumAllowedDateTime))
            throw new ReservationTooSoonException(getId(), bookInAdvance);
    }
}
