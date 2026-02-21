package dev.spiffocode.sigesapi.auth;

import dev.spiffocode.sigesapi.DataTestClass;
import dev.spiffocode.sigesapi.auth.domain.model.LogInAttempt;
import dev.spiffocode.sigesapi.auth.domain.model.LogInAttemptsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataTestClass
class LogInAttemptsRepositoryTest {

    @Autowired
    LogInAttemptsRepository repository;
    @Autowired
    JdbcTemplate jdbc;

    private static final String USERNAME = "user@mail.com";
    private static final String IP = "192.168.1.1";
    private static final LocalDateTime NOW = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
    private static final LocalDateTime WINDOW_START = NOW.minusMinutes(10);

    private LogInAttempt attempt(String username, String ip, boolean success, LocalDateTime timestamp) {
        return LogInAttempt.builder()
                .username(username)
                .ipAddress(ip)
                .success(success)
                .timestamp(timestamp)
                .build();
    }

    private void saveAttempt(String username, String ip, boolean success, LocalDateTime timestamp) {
        jdbc.update(
                "INSERT INTO log_in_attempts (username, ip_address, success, timestamp) VALUES (?, ?, ?, ?)",
                username, ip, success, timestamp
        );
    }

    @Test
    void noAttempts_returnsZero() {
        long count = repository.countRecentFailuresSinceLastSuccess(USERNAME, IP, WINDOW_START);
        assertThat(count).isZero();
    }

    @Test
    void onlySuccesses_returnsZero() {
        repository.save(attempt(USERNAME, IP, true, NOW.minusMinutes(5)));
        repository.save(attempt(USERNAME, IP, true, NOW.minusMinutes(3)));

        long count = repository.countRecentFailuresSinceLastSuccess(USERNAME, IP, WINDOW_START);
        assertThat(count).isZero();
    }

    @Test
    void threeFailuresNoSuccess_returnsThree() {
        repository.save(attempt(USERNAME, IP, false, NOW.minusMinutes(8)));
        repository.save(attempt(USERNAME, IP, false, NOW.minusMinutes(5)));
        repository.save(attempt(USERNAME, IP, false, NOW.minusMinutes(2)));

        long count = repository.countRecentFailuresSinceLastSuccess(USERNAME, IP, WINDOW_START);
        assertThat(count).isEqualTo(3);
    }

    @Test
    void failuresOutsideWindow_notCounted() {
        saveAttempt(USERNAME, IP, false, NOW.minusMinutes(15));
        saveAttempt(USERNAME, IP, false, NOW.minusMinutes(5));

        long count = repository.countRecentFailuresSinceLastSuccess(USERNAME, IP, WINDOW_START);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void failuresBeforeLastSuccess_notCounted() {
        repository.save(attempt(USERNAME, IP, false, NOW.minusMinutes(8)));
        repository.save(attempt(USERNAME, IP, false, NOW.minusMinutes(6)));
        repository.save(attempt(USERNAME, IP, true,  NOW.minusMinutes(4)));
        repository.save(attempt(USERNAME, IP, false, NOW.minusMinutes(2)));

        long count = repository.countRecentFailuresSinceLastSuccess(USERNAME, IP, WINDOW_START);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void multipleSuccesses_onlyCountsFailuresAfterLastOne() {
        repository.save(attempt(USERNAME, IP, false, NOW.minusMinutes(9)));
        repository.save(attempt(USERNAME, IP, true,  NOW.minusMinutes(7)));
        repository.save(attempt(USERNAME, IP, false, NOW.minusMinutes(5)));
        repository.save(attempt(USERNAME, IP, false, NOW.minusMinutes(4)));
        repository.save(attempt(USERNAME, IP, true,  NOW.minusMinutes(3)));
        repository.save(attempt(USERNAME, IP, false, NOW.minusMinutes(1)));

        long count = repository.countRecentFailuresSinceLastSuccess(USERNAME, IP, WINDOW_START);
        assertThat(count).isEqualTo(1);
    }

    @Test
    void failuresFromDifferentUser_notCounted() {
        repository.save(attempt("other@mail.com", IP, false, NOW.minusMinutes(5)));
        repository.save(attempt("other@mail.com", IP, false, NOW.minusMinutes(3)));

        long count = repository.countRecentFailuresSinceLastSuccess(USERNAME, IP, WINDOW_START);
        assertThat(count).isZero();
    }

    @Test
    void failuresFromDifferentIp_notCounted() {
        repository.save(attempt(USERNAME, "10.0.0.1", false, NOW.minusMinutes(5)));
        repository.save(attempt(USERNAME, "10.0.0.1", false, NOW.minusMinutes(3)));

        long count = repository.countRecentFailuresSinceLastSuccess(USERNAME, IP, WINDOW_START);
        assertThat(count).isZero();
    }

    @Test
    void successFromDifferentIp_doesNotResetCount() {
        repository.save(attempt(USERNAME, IP,false, NOW.minusMinutes(8)));
        repository.save(attempt(USERNAME, IP, false, NOW.minusMinutes(6)));
        repository.save(attempt(USERNAME, "10.0.0.1",true, NOW.minusMinutes(4)));
        repository.save(attempt(USERNAME, IP, false, NOW.minusMinutes(2)));

        long count = repository.countRecentFailuresSinceLastSuccess(USERNAME, IP, WINDOW_START);
        assertThat(count).isEqualTo(3);
    }
}