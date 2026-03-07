package dev.spiffocode.sigesapi.reservables.domain.model;

import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
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

import java.time.LocalDateTime;
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

}
