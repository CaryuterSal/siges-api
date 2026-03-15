package dev.spiffocode.sigesapi.agenda.infrastructure.web;

import dev.spiffocode.sigesapi.agenda.application.service.AgendaService;
import dev.spiffocode.sigesapi.agenda.application.service.QueryAvailabilityFilter;
import dev.spiffocode.sigesapi.agenda.presentation.DayAvailabilityItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

// presentation/controller/ReservationController.java
@RestController
@RequestMapping("/reservables/{reservableId}/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar")
public class CalendarController {

    private final AgendaService service;

    @GetMapping
    @Operation(summary = "Query availability of a reservable as available ranges given a date range")
    public List<DayAvailabilityItem> getCalendar(
            @PathVariable @Schema(description = "Equipment or space to which query the availability agenda") Long reservableId,
            @RequestParam @Schema(description = "Date range lower limit") LocalDate from,
            @RequestParam @Schema(description = "Date range upper limit") LocalDate to) {
        QueryAvailabilityFilter filter = QueryAvailabilityFilter.builder()
                .dateFrom(from)
                .dateTo(to)
                .build();
        return service.calculateAvailability(reservableId, filter);
    }
}