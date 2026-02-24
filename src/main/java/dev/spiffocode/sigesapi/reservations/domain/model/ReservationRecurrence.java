package dev.spiffocode.sigesapi.reservations.domain.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import java.time.DayOfWeek;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(
        name = "reservation_recurrence",
        indexes = {
            @Index(columnList = "day_of_week")
        })
@Entity
public class ReservationRecurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

    @ManyToOne(
            optional = false,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private Reservation reservation;

}
