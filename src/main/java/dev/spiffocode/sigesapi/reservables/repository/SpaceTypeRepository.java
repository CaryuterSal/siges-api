package dev.spiffocode.sigesapi.reservables.repository;

import dev.spiffocode.sigesapi.reservables.model.SpaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceTypeRepository extends JpaRepository<SpaceType, Long> {
}
