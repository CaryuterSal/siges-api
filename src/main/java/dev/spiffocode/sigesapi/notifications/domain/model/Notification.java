package dev.spiffocode.sigesapi.notifications.domain.model;

import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.users.domain.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@Table(name = "notifications")
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String body;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @ManyToOne
    private Reservation relatedReservation;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Type type;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ReadStatus readStatus = ReadStatus.UNREAD;

    @ManyToOne(optional = false, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private User user;

    @ElementCollection
    @Builder.Default
    @CollectionTable(name = "notifications_metadata")
    private Map<String, String> metadata = new HashMap<>();

    @ManyToMany
    private List<PushToken> sentToTokens;

}
