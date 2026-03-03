package dev.spiffocode.sigesapi.reservations.domain.model;

import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.users.domain.model.Applicant;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@Audited
@Table(
        name = "reservations",
        indexes = {
                @Index(columnList = "start_time, end_time"),
                @Index(columnList = "date_from, date_to"),
                @Index(columnList = "status")
        }
)
@Entity
public class Reservation {

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

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    @FutureOrPresent
    private LocalDate dateFrom;

    @NotNull
    @FutureOrPresent
    private LocalDate dateTo;

    private LocalDateTime approvedAt;

    @Builder.Default
    @OneToMany(
            mappedBy = "reservation",
            cascade = CascadeType.ALL
    )
    private List<Note> notes = new ArrayList<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "reservation",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST}
    )
    private List<ReservationRecurrence> recurrences = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    public void approve(Clock clock) {
        if (this.status == Status.APPROVED) {
            throw new IllegalStateException("La reserva ya está aprobada.");
        }

        this.status = Status.APPROVED;
        this.approvedAt = LocalDateTime.now(clock);
    }
}
