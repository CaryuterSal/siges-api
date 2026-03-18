package dev.spiffocode.sigesapi.reports.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

@Entity
@Table(name = "v_resource_stats")
@Immutable
@Getter
@NoArgsConstructor
public class ResourceStats {

    @Id
    private Long reservableId;

    private String resourceName;
    private String resourceStatus;
    private String resourceType;

    private Long totalReservations;
    private Long reservationsThisMonth;
    private BigDecimal occupancyRate;
    private BigDecimal avgDaysBetweenReservations;
}
