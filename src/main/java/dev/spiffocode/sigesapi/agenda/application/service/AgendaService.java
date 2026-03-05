package dev.spiffocode.sigesapi.agenda.application.service;

import dev.spiffocode.sigesapi.agenda.presentation.DayAvailabilityItem;

import java.util.List;

public interface AgendaService {
    List<DayAvailabilityItem> calculateAvailability(Long id, QueryAvailabilityFilter filter);
}
