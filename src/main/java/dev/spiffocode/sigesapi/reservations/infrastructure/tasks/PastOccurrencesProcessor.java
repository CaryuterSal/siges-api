package dev.spiffocode.sigesapi.reservations.infrastructure.tasks;

import dev.spiffocode.sigesapi.reservations.domain.model.OccurrenceStatus;
import dev.spiffocode.sigesapi.reservations.domain.model.RecurringReservation;
import dev.spiffocode.sigesapi.reservations.domain.model.ReservationOccurrence;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class PastOccurrencesProcessor {


    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void processPastOccurrences() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

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
                        .materializedAt(LocalDateTime.now())
                        .build();

                occurrenceRepository.save(occurrence);
            }
        });
    }
}
