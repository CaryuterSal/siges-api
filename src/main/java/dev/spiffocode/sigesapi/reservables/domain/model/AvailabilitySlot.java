package dev.spiffocode.sigesapi.reservables.domain.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.envers.Audited;

import java.util.List;

@Audited
@Builder(toBuilder = true)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "availability_slots")
@Entity
public class AvailabilitySlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            cascade = {CascadeType.MERGE, CascadeType.PERSIST},
            optional = false
    )
    @NotNull
    private Reservable reservable;

    @OneToMany(
            cascade = CascadeType.ALL,
            mappedBy = "group",
            orphanRemoval = true
    )
    @NotEmpty
    private List<Availability> members;
}
