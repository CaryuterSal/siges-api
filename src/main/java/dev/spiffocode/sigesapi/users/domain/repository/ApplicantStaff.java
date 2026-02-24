package dev.spiffocode.sigesapi.users.domain.repository;

import dev.spiffocode.sigesapi.users.domain.model.Applicant;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicantStaff extends CommonUserRepository<Applicant> {
}
