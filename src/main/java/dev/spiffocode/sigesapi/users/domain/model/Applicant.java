package dev.spiffocode.sigesapi.users.domain.model;

import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.envers.Audited;

@Entity
@Getter
@Setter
@ToString
@Audited
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Applicant extends User {
}
