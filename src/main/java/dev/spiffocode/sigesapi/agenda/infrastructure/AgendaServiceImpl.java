package dev.spiffocode.sigesapi.agenda.infrastructure;

import dev.spiffocode.sigesapi.agenda.application.service.AgendaService;
import dev.spiffocode.sigesapi.agenda.application.service.QueryAvailabilityFilter;
import dev.spiffocode.sigesapi.agenda.presentation.DayAvailabilityItem;

import java.util.List;

public class AgendaServiceImpl implements AgendaService {
    @Override
    public List<DayAvailabilityItem> calculateAvailability(Long id, QueryAvailabilityFilter filter) {
        return List.of();
    }
}
