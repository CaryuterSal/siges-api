package dev.spiffocode.sigesapi.reservables.domain.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@Table(name = "equipments")
@PrimaryKeyJoinColumn(name = "id")
public class Equipment extends Reservable {

    @Column(nullable = false)
    private Integer inventory;

    @ManyToOne
    @JoinColumn(name = "spaces_id")
    private Space space;

}
