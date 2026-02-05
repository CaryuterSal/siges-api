package dev.spiffocode.sigesapi.logbook.model;

import jakarta.persistence.PrePersist;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.RequestInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Optional;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class CustomRevisionListener {

    private final Supplier<Optional<HttpServletRequest>> requestInfoSupplier;

    @PrePersist
    private void onPersist(CustomRevisionEntity entity) {
        var info = requestInfoSupplier.get();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (info.isEmpty() || authentication == null || authentication.getPrincipal() == null) {
            return;
        }

        entity.setRemoteHost(info.get().getRemoteHost());
        entity.setRemoteUser(authentication.getPrincipal().toString());
    }
}