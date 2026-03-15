package dev.spiffocode.sigesapi.reservables.domain.model;

import dev.spiffocode.sigesapi.reservations.domain.exception.ReservableHasAvailabilityExceptionException;
import dev.spiffocode.sigesapi.reservations.domain.exception.ReservableNotAvailableException;
import dev.spiffocode.sigesapi.reservations.domain.exception.ReservableNotAvailableForStudentsException;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.infrastructure.service.impl.ReservableNotAvailableAtRequestedTimeException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@Table(name = "reservables")
@Audited
@FilterDef(name = "softDeleteFilter", defaultCondition = "deleted_at IS NULL")
@Filter(name = "softDeleteFilter")
@EntityListeners(AuditingEntityListener.class)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Reservable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String name;

    @NotNull
    @Builder.Default
    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private ReservableStatus status = ReservableStatus.AVAILABLE;


    @NotBlank
    @Column(length = 400)
    private String description;


    @NotNull
    @Column(nullable = false)
    private boolean studentsAvailable;

    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}
    )
    @JoinColumn(name = "buildings_id")
    @ToString.Exclude
    private Building building;


    @Builder.Default
    @OneToMany(
            mappedBy = "reservable",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST},
            orphanRemoval = true
    )
    List<AvailabilitySlot> availability = new ArrayList<>();

    public void addAvailabilitySlot(AvailabilitySlot availabilitySlot){
        availability.add(availabilitySlot);
        availabilitySlot.setReservable(this);
    }

    @Builder.Default
    @OneToMany(
            mappedBy = "reservable",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST},
            orphanRemoval = true
    )
    List<AvailabilityException> availabilityExceptions = new ArrayList<>();

    public void addAvailabilityException(AvailabilityException availabilityException){
        availabilityExceptions.add(availabilityException);
        availabilityException.setReservable(this);
    }

    @Builder.Default
    @OneToMany(
            mappedBy = "reservable",
            cascade = {CascadeType.ALL}
    )
    List<Reservation> reservations = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @Column(insertable = false, updatable = false)
    private LocalDateTime deletedAt;


    public void assertCanDoReservation(LocalDate requestedDate, LocalTime startTime, LocalTime endTime, boolean isStudent, Clock clock){
        if (getStatus() != ReservableStatus.AVAILABLE)
            throw new ReservableNotAvailableException(getId());
        assertSpecificCanDoReservation(requestedDate, startTime, endTime, clock);
        assertAvailabilityAllowsReservation(requestedDate, startTime, endTime);
        assertUserHasPermission(isStudent);
    }

    public void assertAvailabilityAllowsReservation(LocalDate requestedDate, LocalTime startTime, LocalTime endTime){
        DayOfWeek requestedDay = requestedDate.getDayOfWeek();
        boolean withinSchedule = getAvailability().stream()
                .flatMap(slot -> slot.getMembers().stream())
                .filter(av -> av.getDayOfWeek() == requestedDay)
                .filter(av -> !requestedDate.isBefore(av.getDateFrom()))
                .filter(av -> av.getDateTo() == null || !requestedDate.isAfter(av.getDateTo()))
                .anyMatch(av -> !startTime.isBefore(av.getStartTime()) &&
                        !endTime.isAfter(av.getEndTime()));

        if (!withinSchedule)
            throw new ReservableNotAvailableAtRequestedTimeException(getId(), requestedDate, startTime,
                    endTime);

        boolean hasException = getAvailabilityExceptions().stream()
                .anyMatch(ex -> !requestedDate.isBefore(ex.getDateFrom()) &&
                        !requestedDate.isAfter(ex.getDateTo()) &&
                        !startTime.isBefore(ex.getStartTime()) &&
                        !endTime.isAfter(ex.getEndTime()));

        if (hasException)
            throw new ReservableHasAvailabilityExceptionException(getId(), requestedDate, startTime,
                    endTime);
    }

    private void assertUserHasPermission(boolean isStudent){
        if (isStudent && !isStudentsAvailable())
            throw new ReservableNotAvailableForStudentsException(getId());
    }

    protected  void assertSpecificCanDoReservation(LocalDate requestedDate, LocalTime startTime, LocalTime endTime, Clock clock){
    }

}
