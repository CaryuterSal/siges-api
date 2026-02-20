package dev.spiffocode.sigesapi.auth.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "log_in_attempts")
@Entity
public class LogInAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, updatable = false)
    private String username;

    @NotNull
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @NotNull
    @Pattern(regexp = "(?<!\\S)(?:(?:[1-9]?\\d|1\\d\\d|2[0-4]\\d|25[0-5])\\.?\\b){4}(?!\\S)", message = "Invalid IP address format")
    @Length(max = 15)
    @Column(nullable = false, updatable = false, length = 15)
    private String ipAddress;

    @NotNull
    private Boolean success;
}
