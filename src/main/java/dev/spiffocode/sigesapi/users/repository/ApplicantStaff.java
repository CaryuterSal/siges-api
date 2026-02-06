package dev.spiffocode.sigesapi.users.repository;

import dev.spiffocode.sigesapi.users.model.Applicant;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicantStaff extends CommonUserRepository<Applicant> {
}
