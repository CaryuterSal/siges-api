package dev.spiffocode.sigesapi.reports.infrastructure.controller;

import dev.spiffocode.sigesapi.reports.application.dto.DashboardStatsDto;
import dev.spiffocode.sigesapi.reports.application.dto.ResourceStatsDto;
import dev.spiffocode.sigesapi.reports.application.service.ReportsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Reports and Statistics API")
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get general dashboard statistics")
    public DashboardStatsDto getDashboardStats() {
        return reportsService.getDashboardStats();
    }

    @GetMapping("/resources")
    @Operation(summary = "Get statistics per resource")
    public List<ResourceStatsDto> getResourceStats() {
        return reportsService.getResourceStats();
    }

    @GetMapping("/resources/{id}")
    @Operation(summary = "Get statistics per resource")
    public ResourceStatsDto getResourceStats(@PathVariable Long id) {
        return reportsService.getResourceStats(id);
    }
}
