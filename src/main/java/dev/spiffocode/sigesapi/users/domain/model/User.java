package dev.spiffocode.sigesapi.users.domain.model;

import dev.spiffocode.sigesapi.auth.application.service.CustomUserDetails;
import dev.spiffocode.sigesapi.notifications.domain.model.Notification;
import dev.spiffocode.sigesapi.notifications.domain.model.NotificationPreference;
import dev.spiffocode.sigesapi.notifications.domain.model.PushToken;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@EqualsAndHashCode
@Audited
@Table(name = "app_users", indexes = {
        @Index(columnList = "email"),
        @Index(columnList = "phone_number"),
        @Index(columnList = "first_name, last_name"),
        @Index(columnList = "deleted_at")
})
@EntityListeners(AuditingEntityListener.class)
@FilterDef(name = "softDeleteFilter", defaultCondition = "deleted_at IS NULL")
@Filter(name = "softDeleteFilter")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User implements CustomUserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private Integer tokenVersion = 0;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    @Setter(AccessLevel.NONE)
    private String email;

    @NotBlank
    @Pattern(regexp = "^(\\+\\d{1,2}\\s?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$")
    @Column(nullable = false, unique = true)
    @Setter(AccessLevel.NONE)
    private String phoneNumber;

    @NotBlank
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Column(nullable = false)
    private String lastName;

    @NotNull
    @Column(nullable = false)
    @Past
    private LocalDate birthDate;

    @NotNull
    @Column(nullable = false)
    private String password;

    private LocalDateTime lastLogin;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(insertable = false, updatable = false)
    private LocalDateTime deletedAt;

    @Builder.Default
    @NotAudited
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Notification> notifications = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationPreference> notificationPreferences = new ArrayList<>();

    @Builder.Default
    @NotAudited
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<PushToken> tokens = new ArrayList<>();

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = RoleAuthority.fromClazz(getClass()).getAuthority();
        SimpleGrantedAuthority roleAuthority = new SimpleGrantedAuthority(authority);
        return List.of(roleAuthority);
    }

    @Override
    public @NonNull String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return deletedAt == null;
    }

    @Transient
    public String fullName() {
        return firstName + " " + lastName;
    }

    public void recordLogin(Clock clock) {
        this.lastLogin = LocalDateTime.now(clock);
    }

    public boolean changeEmail(String newEmail, boolean changeVersion) {

        String old = this.email;
        this.email = newEmail;
        if (!Objects.equals(old, newEmail) && changeVersion) {
            setTokenVersion(getTokenVersion() + 1);
            return true;
        }
        return false;
    }

    public boolean changePhoneNumber(String phoneNumber, boolean changeVersion) {

        String old = this.phoneNumber;
        this.phoneNumber = phoneNumber;
        if (!Objects.equals(old, phoneNumber) && changeVersion) {
            setTokenVersion(getTokenVersion() + 1);
            return true;
        }
        return false;
    }

    public boolean changePassword(String password) {
        String old = this.password;
        this.password = password;
        setTokenVersion(getTokenVersion() + 1);
        return Objects.equals(old, password);
    }

}
