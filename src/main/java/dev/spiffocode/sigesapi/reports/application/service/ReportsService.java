package dev.spiffocode.sigesapi.reports.application.service;

import dev.spiffocode.sigesapi.reports.application.dto.DashboardStatsDto;
import dev.spiffocode.sigesapi.reports.application.dto.ResourceStatsDto;
import dev.spiffocode.sigesapi.reports.application.mapper.ReportsMapper;
import dev.spiffocode.sigesapi.reports.domain.model.ResourceStats;
import dev.spiffocode.sigesapi.reports.domain.repository.DashboardStatsRepository;
import dev.spiffocode.sigesapi.reports.domain.repository.ResourceStatsRepository;
import dev.spiffocode.sigesapi.reservables.domain.exception.ReservableNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportsService {

    private final DashboardStatsRepository dashboardStatsRepository;
    private final ResourceStatsRepository resourceStatsRepository;
    private final ReportsMapper reportsMapper;

    public DashboardStatsDto getDashboardStats() {
        return dashboardStatsRepository.getStats()
                .map(reportsMapper::toDto)
                .orElse(DashboardStatsDto.builder().build());
    }

    public List<ResourceStatsDto> getResourceStats() {
        return resourceStatsRepository.findAll().stream()
                .map(reportsMapper::toDto)
                .collect(Collectors.toList());
    }

    public ResourceStatsDto getResourceStats(Long id) {
        ResourceStats stats = resourceStatsRepository.findById(id)
                .orElseThrow(() -> new ReservableNotFoundException(id));
        return reportsMapper.toDto(stats);
    }
}
