package dev.spiffocode.sigesapi.logbook.domain.model;

import jakarta.persistence.PrePersist;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomRevisionListener {

    private HttpServletRequest requestInfoSupplier;

    @PrePersist
    private void onPersist(CustomRevisionEntity entity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ( authentication == null || authentication.getPrincipal() == null) {
            return;
        }

        entity.setRemoteHost(requestInfoSupplier.getRemoteHost());
        entity.setRemoteUser(authentication.getPrincipal().toString());
    }
}