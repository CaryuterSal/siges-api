package dev.spiffocode.sigesapi.reservables.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

@Audited
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@Table(name = "equipments")
@PrimaryKeyJoinColumn(name = "id")
public class Equipment extends Reservable {


    @ManyToOne(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.MERGE, CascadeType.PERSIST}
    )
    @JoinColumn(name = "spaces_id")
    private Space space;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    private EquipmentType type;

    public void attachSpace(Space space) {
        this.space = space;
        space.getEquipments().add(this);
    }

    @NotBlank
    @Column(unique = true)
    private String inventoryNum;

}
