package dev.spiffocode.sigesapi.reservations.infrastructure.service.impl;

// application/usecase/GetReservationsUseCase.java
@UseCase
@RequiredArgsConstructor
public class GetReservationsUseCase {

    private final ReservationRepository reservationRepository;
    private final ReservationOccurrenceRepository occurrenceRepository;
    private final RecurrenceDateGenerator recurrenceDateGenerator;

    public Page<ReservationSummaryDTO> execute(GetReservationsQuery query) {

        Pageable pageable = buildPageable(query);

        Page<Reservation> reservations = query.applicantId() != null
            ? reservationRepository.findByApplicantAndFilters(query, pageable)
            : reservationRepository.findByFilters(query, pageable);

        return reservations.map(r -> toSummaryDTO(r, query));
    }

    // -------------------------
    // Historial completo (SCRUM-98)
    // Incluye ocurrencias pasadas de series activas
    // -------------------------

    public Page<ReservationHistoryDTO> getHistory(GetReservationsQuery query) {

        // Reservaciones cerradas (FINISHED, CANCELLED, REJECTED)
        Page<Reservation> closed = query.applicantId() != null
            ? reservationRepository.findByApplicantAndFilters(query, buildPageable(query))
            : reservationRepository.findByFilters(query, buildPageable(query));

        // Ocurrencias pasadas de series que siguen APPROVED
        List<ReservationOccurrence> pastOccurrences = query.applicantId() != null
            ? occurrenceRepository.findByApplicantAndDateRange(
                query.applicantId(), query.dateFrom(), query.dateTo())
            : occurrenceRepository.findByDateRange(
                query.dateFrom(), query.dateTo());

        // Mezclar y ordenar por fecha descendente
        List<ReservationHistoryDTO> combined = new ArrayList<>();
        closed.forEach(r -> combined.add(ReservationHistoryDTO.fromReservation(r)));
        pastOccurrences.forEach(o -> combined.add(ReservationHistoryDTO.fromOccurrence(o)));

        combined.sort(Comparator.comparing(ReservationHistoryDTO::date).reversed());

        // Paginar manualmente el resultado combinado
        int start = query.page() * query.size();
        int end = Math.min(start + query.size(), combined.size());
        List<ReservationHistoryDTO> paged = start >= combined.size()
            ? List.of()
            : combined.subList(start, end);

        return new PageImpl<>(paged, buildPageable(query), combined.size());
    }

    // -------------------------
    // Mapeo a DTOs
    // -------------------------

    private ReservationSummaryDTO toSummaryDTO(Reservation reservation, GetReservationsQuery query) {
        if (reservation instanceof SingleReservation single) {
            return ReservationSummaryDTO.fromSingle(single);
        }

        if (reservation instanceof RecurringReservation recurring) {
            // Calcular próxima ocurrencia para mostrar en el listado
            LocalDate nextOccurrence = findNextOccurrence(recurring);
            return ReservationSummaryDTO.fromRecurring(recurring, nextOccurrence);
        }

        throw new IllegalStateException("Tipo de reservación desconocido: " + reservation.getClass());
    }

    private LocalDate findNextOccurrence(RecurringReservation reservation) {
        LocalDate today = LocalDate.now();

        Set<LocalDate> excludedDates = reservation.getExceptions().stream()
            .map(ReservationException::getExcludedDate)
            .collect(Collectors.toSet());

        return recurrenceDateGenerator.generate(
                reservation.getFrequency(),
                reservation.getRecurrences().stream()
                    .map(ReservationRecurrence::getDayOfWeek)
                    .toList(),
                today.isAfter(reservation.getSeriesDateFrom())
                    ? today
                    : reservation.getSeriesDateFrom(),
                reservation.getSeriesDateTo()
            )
            .stream()
            .filter(date -> !excludedDates.contains(date))
            .findFirst()
            .orElse(null);
    }

    private Pageable buildPageable(GetReservationsQuery query) {
        Sort sort = query.sortDirection().equalsIgnoreCase("ASC")
            ? Sort.by(query.sortBy()).ascending()
            : Sort.by(query.sortBy()).descending();
        return PageRequest.of(query.page(), query.size(), sort);
    }
}