package dev.spiffocode.sigesapi.reservations.domain.model;

import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.users.domain.model.Applicant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter(AccessLevel.PROTECTED)
@ToString
@EntityListeners(AuditingEntityListener.class)
@Audited
@Table(
        name = "reservations"
)
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            optional = false,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private Applicant applicant;

    @ManyToOne(
            optional = false,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private Reservable reservable;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private LocalDateTime approvedAt;

    @Builder.Default
    @OneToMany(
            mappedBy = "reservation",
            cascade = CascadeType.ALL
    )
    private List<Note> notes = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    public abstract void approve(Clock clock);
    public abstract void reject(Clock clock, String reason);
    public abstract void cancel(Clock clock, String reason);
}
