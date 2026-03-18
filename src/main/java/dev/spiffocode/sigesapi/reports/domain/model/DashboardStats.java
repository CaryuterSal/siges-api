package dev.spiffocode.sigesapi.reports.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Table(name = "v_dashboard_stats")
@Immutable
@Getter
@NoArgsConstructor
public class DashboardStats {

    @Id
    @Column(insertable = false, updatable = false)
    private Integer id;

    private Integer pendingRequests;
    private Integer pendingRequestsToday;
    private BigDecimal pendingRequestsPercentage;
    private Integer pendingRequestsDiffYesterday;

    private Integer availableSpaces;
    private Integer totalSpaces;
    private BigDecimal availableSpacesPercentage;
    private Integer availableSpacesDiffYesterday;

    private Integer inUseEquipments;
    private Integer totalEquipments;
    private BigDecimal inUseEquipmentsPercentage;
    private Integer inUseEquipmentsDiffYesterday;

    private Integer todayReservations;
    @Column(name = "avg_daily_reservations_30d")
    private BigDecimal avgDailyReservations30d;
    private BigDecimal todayReservationsDiffAvg;
    private Integer reservationsThisMonth;
}
