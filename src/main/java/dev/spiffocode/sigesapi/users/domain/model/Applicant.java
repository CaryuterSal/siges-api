package dev.spiffocode.sigesapi.users.domain.model;

import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@Audited
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public abstract class Applicant extends User {

    @Builder.Default
    @OneToMany(mappedBy = "petitioner", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Reservation> reservations = new ArrayList<>();
}
