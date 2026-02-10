package dev.spiffocode.sigesapi.reservables.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Reservable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private ReservableStatus status;

    @Column(nullable = false, length = 400)
    private String description;

    @Column(nullable = false)
    private boolean studentsAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buildings_id")
    @ToString.Exclude
    private Building building;

    @Column
    private LocalDateTime deletedAt;

}
