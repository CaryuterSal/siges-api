package dev.spiffocode.sigesapi.reports.domain.repository;

import dev.spiffocode.sigesapi.common.domain.repository.ReadOnlyRepository;
import dev.spiffocode.sigesapi.reports.domain.model.DashboardStats;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DashboardStatsRepository extends ReadOnlyRepository<DashboardStats, Integer> {

    @Query("SELECT d FROM DashboardStats d")
    Optional<DashboardStats> getStats();
}
