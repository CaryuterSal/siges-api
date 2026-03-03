package dev.spiffocode.sigesapi.users.domain.repository;

import dev.spiffocode.sigesapi.users.domain.model.PasswordRecoveryToken;
import dev.spiffocode.sigesapi.users.domain.model.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordRecoveryTokenRepository extends JpaRepository<@NonNull PasswordRecoveryToken, @NonNull Long> {
    Optional<PasswordRecoveryToken> findByJti(String jti);
    void deleteByUserAndUsedFalse(User user);

}
