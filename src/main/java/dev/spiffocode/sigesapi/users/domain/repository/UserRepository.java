package dev.spiffocode.sigesapi.users.domain.repository;

import dev.spiffocode.sigesapi.users.domain.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends CommonUserRepository<User> {

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

}
