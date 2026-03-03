package dev.spiffocode.sigesapi.users.domain.repository;

import dev.spiffocode.sigesapi.users.domain.model.Admin;
import lombok.NonNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends CommonUserRepository<Admin>, RevisionRepository<@NonNull Admin, @NonNull Long, @NonNull Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE app_users
        SET deleted_at = NOW()
        FROM admins a
        WHERE app_users.id = a.id AND app_users.id = :id AND deleted_at IS NULL
    """, nativeQuery = true)
    int softDeleteById(@Param("id") Long id);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE app_users
        SET deleted_at = NULL
        FROM admins a
        WHERE app_users.id = a.id AND app_users.id = :id AND deleted_at IS NOT NULL
    """, nativeQuery = true)
    int restore(@Param("id") Long id);
}
