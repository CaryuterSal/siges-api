package dev.spiffocode.sigesapi.reports.domain.repository;

import dev.spiffocode.sigesapi.common.domain.repository.ReadOnlyRepository;
import dev.spiffocode.sigesapi.reports.domain.model.ResourceStats;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceStatsRepository extends ReadOnlyRepository<ResourceStats, Long> {
}
