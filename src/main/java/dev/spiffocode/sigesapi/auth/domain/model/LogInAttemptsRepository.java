package dev.spiffocode.sigesapi.auth.domain.model;

import dev.spiffocode.sigesapi.common.domain.repository.ImmutableRepository;
import io.lettuce.core.dynamic.annotation.Param;
import lombok.NonNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LogInAttemptsRepository extends ImmutableRepository<@NonNull LogInAttempt, @NonNull Long> {

    @Query("""
    SELECT COUNT(a) FROM LogInAttempt a
    WHERE a.username = :username
      AND a.ipAddress = :ipAddress
      AND a.success = false
      AND a.timestamp > :windowStart
      AND a.timestamp > COALESCE(
          (SELECT MAX(s.timestamp) FROM LogInAttempt s
           WHERE s.username = :username AND s.ipAddress = :ipAddress AND s.success = true),
          :windowStart
      )
    """)
    long countRecentFailuresSinceLastSuccess(@Param("username") String username, @Param("ipAddress") String ipAddress, @Param("windowStart") LocalDateTime windowStart);


}
