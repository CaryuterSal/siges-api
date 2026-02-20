package dev.spiffocode.sigesapi.reservables.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Table(name = "reservables")
@Audited
@SoftDelete(strategy = SoftDeleteType.TIMESTAMP, columnName = "deleted_at")
@EntityListeners(AuditingEntityListener.class)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Reservable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private ReservableStatus status;

    @Column(nullable = false, length = 400)
    private String description;

    @Column(nullable = false)
    private boolean studentsAvailable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buildings_id")
    @ToString.Exclude
    private Building building;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @CreatedBy
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdBy;

}
