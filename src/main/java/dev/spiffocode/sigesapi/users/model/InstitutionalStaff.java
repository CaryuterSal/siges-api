package dev.spiffocode.sigesapi.users.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InstitutionalStaff extends Applicant{

    @Column(nullable = false, unique = true)
    private String employeeNumber;
}
