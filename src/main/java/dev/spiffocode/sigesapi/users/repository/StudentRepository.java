package dev.spiffocode.sigesapi.users.repository;

import dev.spiffocode.sigesapi.users.model.Student;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends CommonUserRepository<Student> {
    Optional<Student> findByRegistrationNumber(String registrationNumber);
}
