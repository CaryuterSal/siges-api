package dev.spiffocode.sigesapi.users.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;
import org.hibernate.envers.Audited;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@EqualsAndHashCode
@Audited
@Table(name = "app_users", indexes = {
        @Index(columnList = "email"),
        @Index(columnList = "phone_number"),
        @Index(columnList = "first_name, last_name"),
        @Index(columnList = "deleted_at")
})
@SoftDelete(
        strategy = SoftDeleteType.TIMESTAMP,
        columnName = "deleted_at")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class User implements UserDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Pattern(regexp = "^(\\+\\d{1,2}\\s?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$")
    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    @Past
    private LocalDate birthDate;

    @Column(nullable = false)
    private String password;

    private LocalDateTime lastLogin;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = RoleAuthority.fromClazz(getClass()).getAuthority();
        SimpleGrantedAuthority roleAuthority = new SimpleGrantedAuthority(authority);
        return List.of(roleAuthority);
    }

    @Override
    public @NonNull String getUsername() {
        return email;
    }
}
