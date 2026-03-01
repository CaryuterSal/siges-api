package dev.spiffocode.sigesapi.users.domain.repository;

import dev.spiffocode.sigesapi.users.domain.model.InstitutionalStaff;
import lombok.NonNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstitutionalStaffRepository extends CommonUserRepository<InstitutionalStaff>, RevisionRepository<@NonNull InstitutionalStaff, @NonNull Long, @NonNull Long> {
    Optional<InstitutionalStaff> findByEmployeeNumber(String employeeNumber);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE app_users
        SET deleted_at = NOW()
        FROM institutional_staff isf
        WHERE app_users.id = isf.id AND app_users.id = :id AND deleted_at IS NULL
    """, nativeQuery = true)
    int softDeleteById(@Param("id") Long id);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE app_users
        SET deleted_at = NULL
        FROM institutional_staff isf
        WHERE app_users.id = isf.id AND app_users.id = :id AND deleted_at IS NOT NULL
    """, nativeQuery = true)
    int restore(@Param("id") Long id);
}
