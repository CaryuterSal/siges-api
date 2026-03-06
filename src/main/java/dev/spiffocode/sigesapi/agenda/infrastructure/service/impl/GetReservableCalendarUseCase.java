package dev.spiffocode.sigesapi.agenda.infrastructure.service.impl;

import dev.spiffocode.sigesapi.agenda.application.service.AgendaService;
import dev.spiffocode.sigesapi.agenda.application.service.QueryAvailabilityFilter;
import dev.spiffocode.sigesapi.agenda.presentation.DayAvailabilityItem;
import dev.spiffocode.sigesapi.agenda.presentation.OccupiedBlockItem;
import dev.spiffocode.sigesapi.agenda.presentation.TimeBlockItem;
import dev.spiffocode.sigesapi.reservables.domain.exception.ReservableNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservables.domain.repository.ReservableRepository;
import dev.spiffocode.sigesapi.reservations.domain.model.*;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.stream.Collectors.toSet;

// application/usecase/GetReservableCalendarUseCase.java
@Service
@RequiredArgsConstructor
public class GetReservableCalendarUseCase implements AgendaService {

    private final ReservationRepository reservationRepository;
    private final ReservableRepository reservableRepository;

    public List<DayAvailabilityItem> calculateAvailability(Long reservableId, QueryAvailabilityFilter filter) {
        LocalDate from = filter.dateFrom();
        LocalDate to = filter.dateTo();

        Reservable reservable = reservableRepository.findById(reservableId)
            .orElseThrow(() -> new ReservableNotFoundException(reservableId));

        List<SingleReservation> singles = reservationRepository
            .findActiveSingleByReservableAndDateRange(reservableId, from, to);

        List<RecurringReservation> recurrings = reservationRepository
            .findActiveRecurringByReservableAndDateRange(reservableId, from, to);

        // Construir mapa de ocurrencias por día
        Map<LocalDate, List<OccupiedBlockItem>> occupiedByDay = new HashMap<>();

        // Agregar reservaciones únicas
        singles.forEach(single ->
            occupiedByDay.computeIfAbsent(single.getDate(), k -> new ArrayList<>())
                .add(OccupiedBlockItem.from(single))
        );

        // Expandir series recurrentes en sus fechas concretas
        recurrings.forEach(series -> {
            Set<LocalDate> excludedDates = series.getExceptions().stream()
                .map(ReservationException::getExcludedDate)
                .collect(toSet());

            generateDatesInRange(series, from, to).stream()
                .filter(date -> !excludedDates.contains(date))
                .forEach(date ->
                    occupiedByDay.computeIfAbsent(date, k -> new ArrayList<>())
                        .add(OccupiedBlockItem.from(series, date))
                );
        });

        // Construir el DTO por día
        return from.datesUntil(to.plusDays(1))
            .map(date -> DayAvailabilityItem.builder()
                .date(date)
                .occupiedBlocks(occupiedByDay.getOrDefault(date, List.of()))
                .availableBlocks(calculateAvailableBlocks(
                    reservable.getOpenTime(),
                    reservable.getCloseTime(),
                    occupiedByDay.getOrDefault(date, List.of())
                ))
                .build())
            .toList();
    }

    private List<LocalDate> generateDatesInRange(
            RecurringReservation series, LocalDate from, LocalDate to) {

        LocalDate effectiveFrom = series.getSeriesDateFrom().isBefore(from)
            ? from : series.getSeriesDateFrom();
        LocalDate effectiveTo = series.getSeriesDateTo().isAfter(to)
            ? to : series.getSeriesDateTo();

        return switch (series.getFrequency()) {
            case WEEKLY, BIWEEKLY -> generateWeeklyDates(series, effectiveFrom, effectiveTo);
            case MONTHLY -> generateNthMonthDates(series, effectiveFrom, effectiveTo, 1);
            case BIMONTHLY -> generateNthMonthDates(series, effectiveFrom, effectiveTo, 2);
            case QUARTERLY -> generateNthMonthDates(series, effectiveFrom, effectiveTo, 3);
            case SEMIANNUALLY -> generateNthMonthDates(series, effectiveFrom, effectiveTo, 6);
            case ANNUALLY -> generateNthMonthDates(series, effectiveFrom, effectiveTo, 12);
        };
    }

    private List<LocalDate> generateWeeklyDates(
            RecurringReservation series, LocalDate from, LocalDate to) {

        Set<DayOfWeek> days = series.getRecurrences().stream()
            .map(ReservationRecurrence::getDayOfWeek)
            .collect(toSet());

        int step = series.getFrequency() == RecurrenceFrequency.BIWEEKLY ? 2 : 1;
        List<LocalDate> dates = new ArrayList<>();
        LocalDate cursor = from;

        while (!cursor.isAfter(to)) {
            if (days.contains(cursor.getDayOfWeek())) {
                dates.add(cursor);
            }
            cursor = cursor.plusDays(1);

            // Para BIWEEKLY: saltar semana cuando corresponde
            if (series.getFrequency() == RecurrenceFrequency.BIWEEKLY
                    && cursor.getDayOfWeek() == DayOfWeek.MONDAY
                    && isOddWeek(cursor, series.getSeriesDateFrom())) {
                cursor = cursor.plusWeeks(1);
            }
        }
        return dates;
    }

    private List<LocalDate> generateNthMonthDates(
            RecurringReservation series, LocalDate from, LocalDate to, int monthStep) {

        List<LocalDate> dates = new ArrayList<>();
        LocalDate anchor = series.getSeriesDateFrom();
        LocalDate cursor = anchor;

        while (!cursor.isAfter(to)) {
            if (!cursor.isBefore(from)) {
                dates.add(cursor);
            }
            cursor = anchor.plusMonths(
                (long) monthStep * (dates.size() + (cursor.isBefore(from) ? 1 : 0))
            );
        }
        return dates;
    }

    private List<TimeBlockItem> calculateAvailableBlocks(
            LocalTime open, LocalTime close, List<OccupiedBlockItem> occupied) {

        List<OccupiedBlockItem> sorted = occupied.stream()
            .sorted(Comparator.comparing(OccupiedBlockItem::start))
            .toList();

        List<TimeBlockItem> available = new ArrayList<>();
        LocalTime cursor = open;

        for (OccupiedBlockItem block : sorted) {
            if (cursor.isBefore(block.start())) {
                available.add(new TimeBlockItem(cursor, block.start()));
            }
            if (block.end().isAfter(cursor)) {
                cursor = block.end();
            }
        }

        if (cursor.isBefore(close)) {
            available.add(new TimeBlockItem(cursor, close));
        }

        return available;
    }

    private boolean isOddWeek(LocalDate date, LocalDate seriesStart) {
        long weeksBetween = ChronoUnit.WEEKS.between(
            seriesStart.with(DayOfWeek.MONDAY),
            date.with(DayOfWeek.MONDAY)
        );
        return weeksBetween % 2 != 0;
    }
}