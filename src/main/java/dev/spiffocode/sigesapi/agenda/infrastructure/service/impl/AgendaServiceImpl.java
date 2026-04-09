package dev.spiffocode.sigesapi.agenda.infrastructure.service.impl;

import dev.spiffocode.sigesapi.agenda.application.TimeRange;
import dev.spiffocode.sigesapi.agenda.application.service.AgendaService;
import dev.spiffocode.sigesapi.agenda.application.service.QueryAvailabilityFilter;
import dev.spiffocode.sigesapi.agenda.presentation.DayAvailabilityItem;
import dev.spiffocode.sigesapi.agenda.presentation.OccupiedBlockItem;
import dev.spiffocode.sigesapi.agenda.presentation.TimeBlockItem;
import dev.spiffocode.sigesapi.reservables.domain.exception.ReservableNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservables.domain.repository.ReservableRepository;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class AgendaServiceImpl implements AgendaService {

    private final ReservableRepository reservableRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public List<DayAvailabilityItem> calculateAvailability(Long id, QueryAvailabilityFilter filter) {
        Reservable reservable = reservableRepository.findById(id)
                .orElseThrow(() -> new ReservableNotFoundException(id));

        LocalDate dateFrom = filter.dateFrom() != null ? filter.dateFrom() : LocalDate.now();
        LocalDate dateTo = filter.dateTo() != null ? filter.dateTo() : dateFrom.plusMonths(1);

        if (dateTo.isBefore(dateFrom)) {
            throw new IllegalArgumentException("dateTo must be after or equal to dateFrom");
        }

        List<Reservation> reservations = reservationRepository.findByReservableAndDateBetweenAndStatusIn(reservable,
                dateFrom, dateTo, List.of(Status.APPROVED, Status.IN_PROGRESS));

        List<DayAvailabilityItem> availabilityItems = new ArrayList<>();
        LocalDate currentDate = dateFrom;

        while (!currentDate.isAfter(dateTo)) {
            LocalDate finalDate = currentDate;

            List<TimeBlockItem> availableBlocks = new ArrayList<>();

            reservable.getAvailability().stream()
                    .flatMap(slot -> slot.getMembers().stream())
                    .filter(av -> av.getDayOfWeek() == finalDate.getDayOfWeek())
                    .filter(av -> !finalDate.isBefore(av.getDateFrom()))
                    .filter(av -> av.getDateTo() == null || !finalDate.isAfter(av.getDateTo()))
                    .forEach(av -> availableBlocks.add(new TimeBlockItem(av.getStartTime(), av.getEndTime())));

            List<TimeBlockItem> exceptionBlocks = new ArrayList<>();
            reservable.getAvailabilityExceptions().stream()
                    .filter(ex -> !finalDate.isBefore(ex.getDateFrom()) && !finalDate.isAfter(ex.getDateTo()))
                    .forEach(ex -> exceptionBlocks.add(new TimeBlockItem(ex.getStartTime(), ex.getEndTime())));

            // We merge available blocks first for robustness, then subtract exceptions,
            // then merge again
            List<TimeBlockItem> actualAvailableBlocks = mergeTimeBlocks(
                    subtractBlocks(mergeTimeBlocks(availableBlocks), exceptionBlocks,
                            TimeBlockItem.class, TimeBlockItem::new));

            // Compute occupied blocks, then merge adjacent ones
            List<OccupiedBlockItem> occupiedBlocks = mergeOccupiedBlocks(reservations.stream()
                    .filter(r -> r.getDate().equals(finalDate))
                    .map(r -> new OccupiedBlockItem(r.getStartTime(), r.getEndTime(), r.getStatus()))
                    .toList());

            actualAvailableBlocks = subtractBlocks(actualAvailableBlocks, occupiedBlocks,
                    TimeBlockItem.class,
                    TimeBlockItem::new);

            availabilityItems.add(new DayAvailabilityItem(finalDate, actualAvailableBlocks, occupiedBlocks));

            currentDate = currentDate.plusDays(1);
        }

        return availabilityItems;
    }

    private <T extends TimeRange> List<T> subtractBlocks(List<T> baseBlocks, List<? extends TimeRange> exceptions,
            Class<T> blockClazz, BiFunction<LocalTime, LocalTime, T> blockBuilder) {
        if (exceptions.isEmpty())
            return baseBlocks;

        List<T> result = new ArrayList<>(baseBlocks);
        for (TimeRange ex : exceptions) {
            List<T> newResult = new ArrayList<>();
            for (T base : result) {
                LocalTime maxStart = base.start().isAfter(ex.start()) ? base.start() : ex.start();
                LocalTime minEnd = base.end().isBefore(ex.end()) ? base.end() : ex.end();

                if (maxStart.isBefore(minEnd)) {
                    if (base.start().isBefore(maxStart)) {
                        newResult.add(blockBuilder.apply(base.start(), maxStart));
                    }
                    if (base.end().isAfter(minEnd)) {

                        newResult.add(blockBuilder.apply(minEnd, base.end()));
                    }
                } else {
                    newResult.add(base);
                }
            }
            result = newResult;
        }

        return result;
    }

    private List<TimeBlockItem> mergeTimeBlocks(List<TimeBlockItem> blocks) {
        if (blocks == null || blocks.size() <= 1)
            return blocks;

        List<TimeBlockItem> sortedBlocks = new ArrayList<>(blocks);
        sortedBlocks.sort(Comparator.comparing(TimeBlockItem::start));

        List<TimeBlockItem> mergedBlocks = new ArrayList<>();
        TimeBlockItem current = sortedBlocks.getFirst();

        for (int i = 1; i < sortedBlocks.size(); i++) {
            TimeBlockItem next = sortedBlocks.get(i);
            if (!current.end().isBefore(next.start())) {
                LocalTime maxEnd = current.end().isAfter(next.end()) ? current.end() : next.end();
                current = new TimeBlockItem(current.start(), maxEnd);
            } else {
                mergedBlocks.add(current);
                current = next;
            }
        }
        mergedBlocks.add(current);

        return mergedBlocks;
    }

    private List<OccupiedBlockItem> mergeOccupiedBlocks(List<OccupiedBlockItem> blocks) {
        if (blocks == null || blocks.size() <= 1)
            return blocks;

        List<OccupiedBlockItem> sortedBlocks = new ArrayList<>(blocks);
        sortedBlocks.sort(Comparator.comparing(OccupiedBlockItem::start));

        List<OccupiedBlockItem> mergedBlocks = new ArrayList<>();
        OccupiedBlockItem current = sortedBlocks.get(0);

        for (int i = 1; i < sortedBlocks.size(); i++) {
            OccupiedBlockItem next = sortedBlocks.get(i);

            if (!current.end().isBefore(next.start()) && current.status() == next.status()) {
                LocalTime maxEnd = current.end().isAfter(next.end()) ? current.end() : next.end();
                current = new OccupiedBlockItem(current.start(), maxEnd, current.status());
            } else {
                mergedBlocks.add(current);
                current = next;
            }
        }
        mergedBlocks.add(current);

        return mergedBlocks;
    }
}
