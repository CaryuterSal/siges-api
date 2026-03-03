package dev.spiffocode.sigesapi.auth.infrastructure;

import dev.spiffocode.sigesapi.auth.application.service.CustomUserDetails;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SecurityContextHelper {

    public boolean isAuthenticated(){
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }

    public Long getCurrentUserId() {
        return ((CustomUserDetails) Objects.requireNonNull(getAuthentication().getPrincipal())).getId();
    }

    public String getCurrentUserEmail(){
        if (getAuthentication() == null || getAuthentication() instanceof AnonymousAuthenticationToken) {
            return "anonymousUser";
        }
        return ((CustomUserDetails) Objects.requireNonNull(getAuthentication().getPrincipal())).getEmail();
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
        return getCurrentUserId().equals(targetId);
    }

    public boolean isAdminOrCurrentUser(Long targetId) {
        return isAdmin() || isCurrentUser(targetId);
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean hasRole(String role) {
        return getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}