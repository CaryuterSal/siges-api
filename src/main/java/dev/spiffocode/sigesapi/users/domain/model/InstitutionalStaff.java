package dev.spiffocode.sigesapi.users.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Table(indexes = {
        @Index(columnList = "employee_number")
})
public class InstitutionalStaff extends Applicant{

    @NotNull
    @Column(nullable = false, unique = true)
    private String employeeNumber;

    public boolean changeEmployeeNumber(String employeeNumber,  boolean changeVersion){

        String old = this.employeeNumber;
        this.employeeNumber = employeeNumber;
        if(!Objects.equals(old, employeeNumber) && changeVersion){
            setTokenVersion(getTokenVersion() + 1);
            return true;
        }
        return false;
    }
}
