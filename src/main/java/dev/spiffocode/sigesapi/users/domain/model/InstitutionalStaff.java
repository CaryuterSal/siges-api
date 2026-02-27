package dev.spiffocode.sigesapi.users.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;

@Entity
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@SQLDelete(sql = "UPDATE app_users SET deleted_at = NOW() WHERE id = ?")
@Table(indexes = {
        @Index(columnList = "employee_number")
})
public class InstitutionalStaff extends Applicant{


    @NotNull
    @Column(nullable = false, unique = true)
    private String employeeNumber;
}
