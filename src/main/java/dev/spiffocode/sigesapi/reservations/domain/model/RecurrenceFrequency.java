package dev.spiffocode.sigesapi.reservations.domain.model;

public enum RecurrenceFrequency {
    WEEKLY,        // cada semana en los días indicados
    BIWEEKLY,      // cada 2 semanas en los días indicados
    MONTHLY,       // mismo día del mes
    BIMONTHLY,     // cada 2 meses
    QUARTERLY,     // cada 3 meses
    SEMIANNUALLY,  // cada 6 meses
    ANNUALLY       // cada año
}