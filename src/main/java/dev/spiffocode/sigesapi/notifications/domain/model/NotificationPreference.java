package dev.spiffocode.sigesapi.notifications.domain.model;

import dev.spiffocode.sigesapi.users.domain.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user")
@Audited
@Table(name = "notification_preferences", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "type" })
})
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean inAppEnabled = true;
}
