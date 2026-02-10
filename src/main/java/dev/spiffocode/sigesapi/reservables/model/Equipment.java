package dev.spiffocode.sigesapi.reservables.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@PrimaryKeyJoinColumn(name = "id")
public class Equipment extends Reservable {

    @Column(nullable = false)
    private Integer inventory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spaces_id")
    private Space space;

}
