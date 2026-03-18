package dev.spiffocode.sigesapi.reports.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

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
    private Double pendingRequestsPercentage;
    private Integer pendingRequestsDiffYesterday;

    private Integer availableSpaces;
    private Integer totalSpaces;
    private Double availableSpacesPercentage;
    private Integer availableSpacesDiffYesterday;

    private Integer inUseEquipments;
    private Integer totalEquipments;
    private Double inUseEquipmentsPercentage;
    private Integer inUseEquipmentsDiffYesterday;

    private Integer todayReservations;
    private Double avgDailyReservations30d;
    private Integer todayReservationsDiffAvg;
    private Integer reservationsThisMonth;
}
