package dev.spiffocode.sigesapi.logbook.domain.model;

import dev.spiffocode.sigesapi.auth.infrastructure.SecurityContextHelper;
import jakarta.persistence.PrePersist;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class CustomRevisionListener {

    private final SecurityContextHelper securityContextHelper;
    @PrePersist
    private void onPersist(CustomRevisionEntity entity) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs != null) {
            HttpServletRequest curRequest = attrs.getRequest();
            entity.setRemoteHost(curRequest.getRemoteHost());
            entity.setRemoteUser(securityContextHelper.getCurrentUserEmail());
        } else {
            entity.setRemoteHost("SYSTEM_ASYNC");
            String email = securityContextHelper.isAuthenticated() ? securityContextHelper.getCurrentUserEmail() : "SYSTEM";
            entity.setRemoteUser(email);
        }
    }
}