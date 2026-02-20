package dev.spiffocode.sigesapi.reservables.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;

@Entity
@Getter
@Setter
@ToString
@Table(name = "availabilities")
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotNull
    @Column(nullable = false)
    private LocalDate dateFrom;

    @Column
    private LocalDate dateTo;

    @Column
    private LocalTime startTime;

    @Column
    private LocalTime endTime;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;


    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;



    @NotNull
    @ManyToOne
    @JoinColumn(name = "reservable_id", nullable = false)
    private Reservable reservable;

}
