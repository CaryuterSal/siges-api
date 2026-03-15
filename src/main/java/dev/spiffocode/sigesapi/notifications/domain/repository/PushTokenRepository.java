package dev.spiffocode.sigesapi.notifications.domain.repository;

import dev.spiffocode.sigesapi.notifications.domain.model.PushToken;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushTokenRepository extends JpaRepository<@NonNull PushToken,@NonNull String> {
    List<PushToken> findByUserId(@NonNull Long userId);

    Optional<PushToken> findByDeviceId(String deviceId);

    void deleteAllByTokenIn(List<String> invalidTokens);
}
