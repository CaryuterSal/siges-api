package dev.spiffocode.sigesapi.reservables.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Audited
@FilterDef(name = "softDeleteFilter", defaultCondition = "deleted_at IS NULL")
@Filter(name = "softDeleteFilter")
@Table(name = "buildings")
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true, length = 45)
    private String name;

    @Column(insertable = false, updatable = false)
    LocalDateTime deletedAt;

    @Builder.Default
    @Filter(name = "softDeleteFilter")
    @OneToMany(
            mappedBy = "building",
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private List<Reservable> reservables = new ArrayList<>();

    public void addReservable(Reservable reservable) {
        reservables.add(reservable);
        reservable.setBuilding(this);
    }
}
