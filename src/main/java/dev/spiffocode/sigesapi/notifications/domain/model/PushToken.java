package dev.spiffocode.sigesapi.notifications.domain.model;


import dev.spiffocode.sigesapi.users.domain.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
@Table(name = "push_tokens")
@EntityListeners(AuditingEntityListener.class)
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@SoftDelete(strategy = SoftDeleteType.ACTIVE, columnName = "is_active")
@Entity
public class PushToken {

    @Id
    private String token;

    private String deviceId;

    @NotNull
    private Platform platform;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastUsedAt;

    @ManyToOne(
            optional = false,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private User user;
}
