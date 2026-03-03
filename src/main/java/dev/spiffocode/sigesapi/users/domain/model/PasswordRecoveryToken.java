package dev.spiffocode.sigesapi.users.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.data.annotation.CreatedDate;

import java.time.Clock;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_recovery_tokens")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PasswordRecoveryToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String jti;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecoveryPlatform platform;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder.Default
    @Column(nullable = false)
    private boolean used = false;

    @ManyToOne(optional = false)
    private User user;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isExpired(Clock clock) {
        return LocalDateTime.now(clock).isAfter(expiresAt);
    }

    public void markAsUsed() {
        this.used = true;
    }
}