package dev.spiffocode.sigesapi.reservables.domain.repository;

import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceTypeRepository extends JpaRepository<@NonNull SpaceType, @NonNull Long> {
}
