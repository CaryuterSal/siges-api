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
        @Index(columnList = "registration_number")
})
public class Student extends Applicant {

    @NotNull
    @Column(nullable = false, unique = true)
    private String registrationNumber;
    
    public boolean changeRegistrationNumber(String registrationNumber,  boolean changeVersion){

        String old = this.registrationNumber;
        this.registrationNumber = registrationNumber;
        if(!Objects.equals(old, registrationNumber) && changeVersion){
            this.setTokenVersion(getTokenVersion() + 1);
            return true;
        }
        return false;
    }
}
