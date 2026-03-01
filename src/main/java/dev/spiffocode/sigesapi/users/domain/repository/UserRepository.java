package dev.spiffocode.sigesapi.users.domain.repository;

import dev.spiffocode.sigesapi.users.domain.model.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends CommonUserRepository<User>, RevisionRepository<@NonNull User, @NonNull Long, @NonNull Long> {

    @Query("""
    select u
    from User u
    left join Applicant a on a.id = u.id
    left join Student s on s.id = a.id
    left join InstitutionalStaff st on st.id = a.id
    where
        u.email = :identifier
        or u.phoneNumber = :identifier
        or s.registrationNumber = :identifier
        or st.employeeNumber = :identifier
    """)
    Optional<User> findByIdentifier(String identifier);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE app_users SET deleted_at = NOW() WHERE id = :id AND deleted_at IS NULL", nativeQuery = true)
    int softDeleteById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE app_users SET deleted_at = NULL WHERE id = :id AND deleted_at IS NOT NULL", nativeQuery = true)
    int restoreById(@Param("id") Long id);

}
