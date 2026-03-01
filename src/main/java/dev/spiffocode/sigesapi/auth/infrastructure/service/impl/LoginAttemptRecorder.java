package dev.spiffocode.sigesapi.auth.infrastructure.service.impl;

import dev.spiffocode.sigesapi.auth.domain.model.LogInAttempt;
import dev.spiffocode.sigesapi.auth.domain.model.LogInAttemptsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptRecorder {

    private final LogInAttemptsRepository logInAttemptsRepository;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String identifier, String ip) {
        logInAttemptsRepository.save(
            LogInAttempt.builder()
                .username(identifier)
                .ipAddress(ip)
                .success(false)
                .timestamp(LocalDateTime.now(clock))
                .build()
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(String identifier, String ip) {
        logInAttemptsRepository.save(
            LogInAttempt.builder()
                .username(identifier)
                .ipAddress(ip)
                .success(true)
                .timestamp(LocalDateTime.now(clock))
                .build()
        );
    }
}