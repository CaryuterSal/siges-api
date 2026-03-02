package dev.spiffocode.sigesapi.users.domain.model;

import dev.spiffocode.sigesapi.notifications.domain.model.Notification;
import dev.spiffocode.sigesapi.notifications.domain.model.PushToken;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
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
public abstract class User implements UserDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Pattern(regexp = "^(\\+\\d{1,2}\\s?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$")
    @Column(nullable = false, unique = true)
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

    @Column(insertable = false, updatable = false)
    private LocalDateTime deletedAt;

    @Builder.Default
    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY
    )
    private List<Notification> notifications = new ArrayList<>();


    @Builder.Default
    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY
    )
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
}
