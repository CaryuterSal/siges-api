package dev.spiffocode.sigesapi.reservables.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "reservables")
public abstract class Reservable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    private String status;

    @Column(nullable = false, length = 400)
    private String description;

    @Column(name = "students_available", nullable = false)
    private boolean studentsAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buildings_id")
    @ToString.Exclude
    private Building building;

    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedAt;

}
