package dev.spiffocode.sigesapi.reservables.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "spaces")
@PrimaryKeyJoinColumn(name = "id")
public class Space extends Reservable{

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "space_types_id", nullable = false)
    private SpaceType type;

}
