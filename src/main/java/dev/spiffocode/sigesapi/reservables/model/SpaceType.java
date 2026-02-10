package dev.spiffocode.sigesapi.reservables.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@Table(name = "space_types")
public class SpaceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 45)
    private String name;

    @Column(nullable = false, length = 400)
    private String description;
}
