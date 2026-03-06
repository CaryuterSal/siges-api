package dev.spiffocode.sigesapi.reservations.infrastructure.service.impl;

// application/dto/query/GetReservationsQuery.java
public record GetReservationsQuery(
    Long applicantId,        // null si es admin viendo todas
    List<Status> statuses,   // filtro por estado
    LocalDate dateFrom,      // filtro por rango de fechas
    LocalDate dateTo,
    Long reservableId,       // filtro por recurso
    int page,
    int size,
    String sortBy,           // campo de ordenación
    String sortDirection     // ASC, DESC
) {
    // Factory methods para los casos comunes
    public static GetReservationsQuery pendingForAdmin(int page, int size) {
        return new GetReservationsQuery(
            null, List.of(Status.PENDING), null, null, null, page, size, "createdAt", "DESC"
        );
    }

    public static GetReservationsQuery historyForApplicant(Long applicantId, int page, int size) {
        return new GetReservationsQuery(
            applicantId,
            List.of(Status.FINISHED, Status.CANCELLED, Status.REJECTED),
            null, null, null, page, size, "createdAt", "DESC"
        );
    }

    public static GetReservationsQuery activeForApplicant(Long applicantId, int page, int size) {
        return new GetReservationsQuery(
            applicantId,
            List.of(Status.PENDING, Status.APPROVED),
            null, null, null, page, size, "createdAt", "DESC"
        );
    }
}