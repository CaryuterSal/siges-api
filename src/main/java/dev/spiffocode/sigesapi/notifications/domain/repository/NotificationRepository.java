package dev.spiffocode.sigesapi.notifications.domain.repository;

import dev.spiffocode.sigesapi.notifications.domain.model.Notification;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<@NonNull Notification, @NonNull Long>, JpaSpecificationExecutor<@NonNull Notification> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdOrderBySentAtDesc(Long userId);
}
