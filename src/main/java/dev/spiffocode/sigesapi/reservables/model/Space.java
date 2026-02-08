package dev.spiffocode.sigesapi.reservables.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "spaces")
@PrimaryKeyJoinColumn(name = "id")
public class Space extends Reservable{

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "space_types_id", nullable = false)
    private SpaceType type;

}
