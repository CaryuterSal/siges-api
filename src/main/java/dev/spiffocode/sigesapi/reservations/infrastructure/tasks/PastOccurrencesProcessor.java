package dev.spiffocode.sigesapi.reservations.infrastructure.tasks;

import dev.spiffocode.sigesapi.reservations.domain.model.OccurrenceStatus;
import dev.spiffocode.sigesapi.reservations.domain.model.RecurringReservation;
import dev.spiffocode.sigesapi.reservations.domain.model.ReservationOccurrence;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PastOccurrencesProcessor {

    private final ReservationRepository reservationRepository;
    private final Clock clock;


    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void processPastOccurrences() {
        LocalDate yesterday = LocalDate.now(clock).minusDays(1);

        List<RecurringReservation> activeSeries = reservationRepository
                .findApprovedRecurringWithOccurrencesOn(yesterday);

        activeSeries.forEach(series -> {
            boolean wasExcluded = series.getExceptions().stream()
                    .anyMatch(e -> e.getExcludedDate().equals(yesterday));

            if (!wasExcluded) {
                ReservationOccurrence occurrence = ReservationOccurrence.builder()
                        .reservation(series)
                        .occurrenceDate(yesterday)
                        .startTime(series.getStartTime())
                        .endTime(series.getEndTime())
                        .status(OccurrenceStatus.COMPLETED)
                        .materializedAt(LocalDateTime.now(clock))
                        .build();

                occurrenceRepository.save(occurrence);
            }
        });
    }
}
