package dev.spiffocode.sigesapi.notifications.domain.model;

import dev.spiffocode.sigesapi.users.domain.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Generated;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Table(name = "push_tokens",
    indexes = {
            @Index(columnList = "device_id")
    })
@EntityListeners(AuditingEntityListener.class)
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@FilterDef(name = "softDeleteFilter")
@Filter(name = "softDeleteFilter", condition = "is_active = TRUE")
@Entity
public class PushToken {

        @Id
        private String token;

        private String deviceId;

        @NotNull
        @Enumerated(EnumType.STRING)
        private Platform platform;

        @CreatedDate
        @Column(nullable = false, updatable = false)
        private LocalDateTime createdAt;

        private LocalDateTime lastUsedAt;

        @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
        private User user;

        @Generated
        @Column(nullable = false, insertable = false, updatable = false)
        private Boolean isActive;

        @ManyToMany(mappedBy = "sentToTokens")
        private List<Notification> notificationsSent;

}
