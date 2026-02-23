package dev.spiffocode.sigesapi.reservables.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    @ManyToOne
    @JoinColumn(name = "spaces_id")
    private Space space;

    @NotBlank
    @Column(unique = true)
    private String inventoryNum;
}
