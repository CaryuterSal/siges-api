package dev.spiffocode.sigesapi.reservables.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Audited
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "equipments")
@Table(name = "equipment_types")
@FilterDef(name = "softDeleteFilter", defaultCondition = "deleted_at IS NULL")
@Filter(name = "softDeleteFilter")
public class EquipmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 45)
    private String name;

    @Column(nullable = false, length = 400)
    private String description;

    @Column(insertable = false, updatable = false)
    private LocalDateTime deletedAt;

    @Builder.Default
    @Filter(name = "softDeleteFilter")
    @OneToMany(mappedBy = "type", cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    private List<Equipment> equipments = new ArrayList<>();

    public void addEquipment(Equipment equipment) {
        equipments.add(equipment);
        equipment.setType(this);
    }


    @Builder.Default
    @Filter(name = "softDeleteFilter")
    @OneToMany(mappedBy = "type", cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    private List<SpaceAsset> spaceAssets = new ArrayList<>();

    public void addSpaceAsset(SpaceAsset spaceAsset) {
        spaceAssets.add(spaceAsset);
        spaceAsset.setType(this);
    }

}
