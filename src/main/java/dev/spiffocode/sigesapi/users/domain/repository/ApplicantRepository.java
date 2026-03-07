package dev.spiffocode.sigesapi.users.domain.repository;

import dev.spiffocode.sigesapi.users.domain.model.Applicant;
import lombok.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicantRepository extends CommonUserRepository<@NonNull Applicant> {
}
