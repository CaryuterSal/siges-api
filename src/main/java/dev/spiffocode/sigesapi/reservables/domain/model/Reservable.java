package dev.spiffocode.sigesapi.reservables.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
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
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buildings_id")
    @ToString.Exclude
    private Building building;


    @OneToMany(
            mappedBy = "reservable",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<AvailabilitySlot> availability;

    @OneToMany(
            mappedBy = "reservable",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<AvailabilityException> availabilityExceptions;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

}
