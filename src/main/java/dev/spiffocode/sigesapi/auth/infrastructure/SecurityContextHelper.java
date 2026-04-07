package dev.spiffocode.sigesapi.auth.infrastructure;

import dev.spiffocode.sigesapi.auth.application.service.CustomUserDetails;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SecurityContextHelper {

    public boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    public Long getCurrentUserId() {
        Authentication authentication = getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null
                || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return null;
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }

    public String getCurrentUserEmail() {
        Authentication authentication = getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken
                || authentication.getPrincipal() == null) {
            return "anonymousUser";
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getEmail();
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isStudent() {
        return hasRole("ROLE_STUDENT");
    }

    public boolean isApplicant() {
        return isStudent() || isInstitutionalStaff();
    }

    public boolean isInstitutionalStaff() {
        return hasRole("ROLE_INSTITUTIONAL_STAFF");
    }

    public boolean isCurrentUser(Long targetId) {
        Long currentId = getCurrentUserId();
        return currentId != null && currentId.equals(targetId);
    }

    public boolean isAdminOrCurrentUser(Long targetId) {
        return isAdmin() || isCurrentUser(targetId);
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean hasRole(String role) {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), role));
    }
}