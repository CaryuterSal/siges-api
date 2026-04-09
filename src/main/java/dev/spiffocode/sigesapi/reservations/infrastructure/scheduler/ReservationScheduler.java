package dev.spiffocode.sigesapi.reservations.infrastructure.scheduler;

import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.domain.repository.ReservableRepository;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;
    private final ReservableRepository reservableRepository;
    private final Clock clock;

    /**
     * Every minute, check for APPROVED reservations that should have started.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void autoStartReservations() {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        List<Reservation> toStart = reservationRepository.findByStatusAndDateAndStartTimeLessThanEqual(
                Status.APPROVED, today, now);

        if (toStart.isEmpty())
            return;

        log.info("Auto-starting {} reservations", toStart.size());

        for (Reservation reservation : toStart) {
            try {
                reservation.start(clock);
                reservation.setAutoStarted(true);
                reservation.getReservable().setStatus(ReservableStatus.LOANED);

                reservableRepository.save(reservation.getReservable());
                reservationRepository.save(reservation);

                log.debug("Reservation {} auto-started", reservation.getId());
            } catch (Exception e) {
                log.error("Failed to auto-start reservation {}: {}", reservation.getId(), e.getMessage());
            }
        }
    }

    /**
     * Every minute, check for IN_PROGRESS reservations that should have finished.
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void autoFinishReservations() {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);

        List<Reservation> toFinish = reservationRepository.findByStatusAndDateAndEndTimeLessThanEqual(
                Status.IN_PROGRESS, today, now);

        if (toFinish.isEmpty())
            return;

        log.info("Auto-finishing {} reservations", toFinish.size());

        for (Reservation reservation : toFinish) {
            try {
                // If it's auto-finished, we don't necessarily know if it was late
                // unless we implement more complex logic. For now, just mark it as finished.
                reservation.finish(clock);
                reservation.setAutoFinished(true);
                reservation.getReservable().setStatus(ReservableStatus.AVAILABLE);

                reservableRepository.save(reservation.getReservable());
                reservationRepository.save(reservation);

                log.debug("Reservation {} auto-finished", reservation.getId());
            } catch (Exception e) {
                log.error("Failed to auto-finish reservation {}: {}", reservation.getId(), e.getMessage());
            }
        }
    }
}
