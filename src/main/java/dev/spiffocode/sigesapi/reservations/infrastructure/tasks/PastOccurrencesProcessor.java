package dev.spiffocode.sigesapi.reservations.infrastructure.tasks;

import dev.spiffocode.sigesapi.reservations.infrastructure.service.impl.FinishReservationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class PastOccurrencesProcessor {

    private final FinishReservationUseCase finishReservationUseCase;

    @Scheduled(cron = "0 0 1 * * *") // cada día a la 1am
    public void processYesterdayOccurrences() {
        log.info("Scheduler: materializando ocurrencias de ayer");
        try {
            finishReservationUseCase.processYesterdayOccurrences();
            log.info("Scheduler: ocurrencias materializadas correctamente");
        } catch (Exception e) {
            log.error("Scheduler: error al materializar ocurrencias", e);
            // No relanzar — el scheduler no debe romperse por errores individuales
        }
    }
}
