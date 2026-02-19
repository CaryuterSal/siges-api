package dev.spiffocode.sigesapi.users.domain.repository;

import dev.spiffocode.sigesapi.users.domain.model.InstitutionalStaff;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstitutionalStaffRepository extends CommonUserRepository<InstitutionalStaff> {
    Optional<InstitutionalStaff> findByEmployeeNumber(String employeeNumber);
}
