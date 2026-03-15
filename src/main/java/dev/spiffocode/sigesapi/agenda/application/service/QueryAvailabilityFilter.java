package dev.spiffocode.sigesapi.agenda.application.service;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record QueryAvailabilityFilter (
        LocalDate dateFrom,
        LocalDate dateTo
){
}
