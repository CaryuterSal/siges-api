package dev.spiffocode.sigesapi.users.domain.repository;

import dev.spiffocode.sigesapi.users.domain.model.Student;
import lombok.NonNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends CommonUserRepository<Student>, RevisionRepository<@NonNull Student, @NonNull Long, @NonNull Long> {
    Optional<Student> findByRegistrationNumber(String registrationNumber);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE app_users
        SET deleted_at = NOW()
        FROM students s
        WHERE app_users.id = s.id AND app_users.id = :id AND deleted_at IS NULL
    """, nativeQuery = true)
    int softDeleteById(@Param("id") Long id);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE app_users
        SET deleted_at = NULL
        FROM students s
        WHERE app_users.id = s.id AND app_users.id = :id AND deleted_at IS NOT NULL
    """, nativeQuery = true)
    int restore(@Param("id") Long id);

    boolean existsByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumberAndIdNot(String registrationNumber, Long id);
}
