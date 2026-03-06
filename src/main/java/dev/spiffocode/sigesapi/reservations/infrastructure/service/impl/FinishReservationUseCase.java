package dev.spiffocode.sigesapi.reservations.infrastructure.service.impl;

import lombok.RequiredArgsConstructor;

// application/usecase/FinishReservationUseCase.java
@UseCase
@RequiredArgsConstructor
public class FinishReservationUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationOccurrenceRepository occurrenceRepository;
    private final Clock clock;

    // -------------------------
    // Acción manual del admin
    // -------------------------

    @Transactional
    public void execute(Long reservationId) {

        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (reservation.getStatus() != Status.APPROVED)
            throw new InvalidReservationStatusException(
                "Solo se puede finalizar una reservación APPROVED. " +
                "Estado actual: " + reservation.getStatus()
            );

        reservation.finish(clock);
        reservationRepository.save(reservation);

        // Si es recurrente, materializar todas las ocurrencias pasadas
        // que no hayan sido materializadas aún
        if (reservation instanceof RecurringReservation recurring) {
            materializePastOccurrences(recurring);
        }
    }

    // -------------------------
    // Scheduler nocturno
    // -------------------------

    @Transactional
    public void processYesterdayOccurrences() {
        LocalDate yesterday = LocalDate.now(clock).minusDays(1);

        List<RecurringReservation> activeSeries = occurrenceRepository
            .findApprovedRecurringWithOccurrenceOn(yesterday);

        activeSeries.forEach(series -> {
            boolean wasExcluded = series.getExceptions().stream()
                .anyMatch(e -> e.getExcludedDate().equals(yesterday));

            if (!wasExcluded) {
                materializeOccurrence(series, yesterday, OccurrenceStatus.COMPLETED);
            }
        });
    }

    // -------------------------
    // Materialización de historial
    // -------------------------

    private void materializePastOccurrences(RecurringReservation series) {
        LocalDate today = LocalDate.now(clock);

        Set<LocalDate> excludedDates = series.getExceptions().stream()