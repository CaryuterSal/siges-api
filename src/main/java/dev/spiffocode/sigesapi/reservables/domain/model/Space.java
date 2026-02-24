package dev.spiffocode.sigesapi.reservables.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@Table(name = "spaces")
@PrimaryKeyJoinColumn(name = "id")
public class Space extends Reservable{


    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "space_types_id", nullable = false)
    private SpaceType type;

}
