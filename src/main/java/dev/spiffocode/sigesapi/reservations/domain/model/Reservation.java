package dev.spiffocode.sigesapi.reservations.domain.model;

import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservations.domain.exception.InvalidReservationStatusException;
import dev.spiffocode.sigesapi.users.domain.model.Applicant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "reservations",
        indexes = {
                @Index(columnList = "status"),
                @Index(columnList = "date, start_time, end_time"),
                @Index(columnList = "reservable_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Audited
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Applicant petitioner;

    @ManyToOne(optional = false)
    private Reservable reservable;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.PENDING;

    @NotNull
    private LocalDate date;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @NotNull
    private GroupingType type;

    @Positive
    private Integer companions;

    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime finishedAt;

    @Builder.Default
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL)
    private List<Note> notes = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    public void approve(Clock clock) {
        if (this.status != Status.PENDING)
            throw new InvalidReservationStatusException(this.status, Status.PENDING);
        this.status = Status.APPROVED;
        this.approvedAt = LocalDateTime.now(clock);
    }

    public void reject(String comment, Clock clock) {
        if (this.status != Status.PENDING)
            throw new InvalidReservationStatusException(this.status, Status.PENDING);
        this.status = Status.REJECTED;
        this.rejectedAt = LocalDateTime.now(clock);
        addNote(comment);
    }

    public void cancel(String reason, Clock clock) {
        if (this.status == Status.FINISHED
                || this.status == Status.REJECTED
                || this.status == Status.CANCELLED)
            throw new InvalidReservationStatusException(this.status, Status.CANCELLED);
        this.status = Status.CANCELLED;
        this.cancelledAt = LocalDateTime.now(clock);
        addNote(reason);
    }


    public void start(Clock clock) {
        if (this.status != Status.APPROVED)
            throw new InvalidReservationStatusException(this.status, Status.APPROVED);
        this.status = Status.IN_PROGRESS;
        this.finishedAt = LocalDateTime.now(clock);
    }

    public void finish(Clock clock) {
        if (this.status != Status.IN_PROGRESS)
            throw new InvalidReservationStatusException(this.status, Status.IN_PROGRESS);
        this.status = Status.FINISHED;
        this.finishedAt = LocalDateTime.now(clock);
    }

    public boolean reschedule(LocalDate date, LocalTime startTime, LocalTime endTime, Clock clock) {
        if (this.getStatus() != Status.PENDING && this.getStatus() != Status.APPROVED)
            throw new InvalidReservationStatusException(this.getStatus(), Status.PENDING);
        if(this.date != date || this.startTime != startTime || this.endTime != endTime){
            this.status = Status.PENDING;
            return true;
        }
        return false;
    }

    public void addNote(String comment) {
        if (comment == null || comment.isBlank()) return;
        this.notes.add(Note.builder()
                .comment(comment)
                .reservation(this)
                .build());
    }
}