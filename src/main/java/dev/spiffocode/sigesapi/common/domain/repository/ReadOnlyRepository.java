package dev.spiffocode.sigesapi.common.domain.repository;

import lombok.NonNull;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface ReadOnlyRepository<@NonNull T, @NonNull ID> extends Repository<@NonNull T, @NonNull ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
}
