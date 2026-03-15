package dev.spiffocode.sigesapi.notifications.domain.specification;

import dev.spiffocode.sigesapi.notifications.application.service.NotificationFilter;
import dev.spiffocode.sigesapi.notifications.domain.model.Notification;
import dev.spiffocode.sigesapi.notifications.domain.model.ReadStatus;
import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

public class NotificationSpecifications {

    public static Specification<@NonNull Notification> byFilter(NotificationFilter filter, Long userId) {
        return Specification
                .where(typeIn(filter.type()))
                .and(forUser(userId))
                .and(withStatus(filter.readStatus()));
    }

    public static Specification<@NonNull Notification> forUser(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<@NonNull Notification> typeIn(Type type) {
        return ((root, query, cb) ->
            type == null ? null :  cb.equal(root.get("type"), type)
        );
    }

    public static Specification<@NonNull Notification> withStatus(ReadStatus status) {
        return ((root, query, cb) ->
                status == null ? null : cb.equal(root.get("readStatus"), status)
        );
    }
}
