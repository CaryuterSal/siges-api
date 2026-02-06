package dev.spiffocode.sigesapi.users.repository;

import dev.spiffocode.sigesapi.users.model.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface CommonUserRepository<U extends User> extends JpaRepository<@NonNull U, @NonNull Long>,
        RevisionRepository<@NonNull U, @NonNull Long, @NonNull Long> {
    Optional<U> findByEmail(String email);
    List<U> findByFirstNameAndLastName(String firstName, String lastName);
}
