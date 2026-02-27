package dev.spiffocode.sigesapi.notifications.domain.model;


import dev.spiffocode.sigesapi.users.domain.model.User;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Generated;
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
@FilterDef(name = "softDeleteFilter", defaultCondition = "is_active = TRUE")
@Filter(name = "softDeleteFilter")
@SQLDelete(sql = "UPDATE push_tokens SET is_active = FALSE WHERE id = ?")
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

    @Generated
    @Column(nullable = false, insertable = false, updatable = false)
    private Boolean isActive;
}
