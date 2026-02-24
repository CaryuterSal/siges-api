package dev.spiffocode.sigesapi.users.domain.repository;

import dev.spiffocode.sigesapi.users.domain.model.Student;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends CommonUserRepository<Student> {
    Optional<Student> findByRegistrationNumber(String registrationNumber);
}
