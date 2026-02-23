package dev.spiffocode.sigesapi.reservables.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Duration;

@Entity
@Getter
@Setter
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

    @NotNull
    @Positive
    private Duration bookInAdvance;

    @NotNull
    @Positive
    private Integer capacity;

}
