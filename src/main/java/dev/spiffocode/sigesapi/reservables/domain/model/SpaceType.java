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
@ToString
@Table(name = "space_types")
@FilterDef(name = "softDeleteFilter", defaultCondition = "deleted_at IS NULL")
@Filter(name = "softDeleteFilter")
public class SpaceType {

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
    @OneToMany(
            mappedBy = "type",
            cascade = {CascadeType.MERGE, CascadeType.PERSIST}
    )
    private List<Space> spaces = new ArrayList<>();


    public void addSpace(Space space) {
        spaces.add(space);
        space.setType(this);
    }

}
