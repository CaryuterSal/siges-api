package dev.spiffocode.sigesapi.reports.application.mapper;

import dev.spiffocode.sigesapi.reports.application.dto.DashboardStatsDto;
import dev.spiffocode.sigesapi.reports.application.dto.ResourceStatsDto;
import dev.spiffocode.sigesapi.reports.domain.model.DashboardStats;
import dev.spiffocode.sigesapi.reports.domain.model.ResourceStats;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReportsMapper {

    DashboardStatsDto toDto(DashboardStats stats);

    ResourceStatsDto toDto(ResourceStats stats);
}
