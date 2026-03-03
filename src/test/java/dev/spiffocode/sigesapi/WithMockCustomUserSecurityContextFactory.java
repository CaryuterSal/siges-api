package dev.spiffocode.sigesapi;

import dev.spiffocode.sigesapi.auth.application.service.CustomUserDetails;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Collection;
import java.util.List;

public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<@NotNull WithMockCustomUser> {

    @Override
    public @NotNull SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        CustomUserDetails userDetails = new CustomUserDetails(){
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of(new SimpleGrantedAuthority(annotation.role()));
            }

            @Override
            public @Nullable String getPassword() {
                return null;
            }

            @Override
            public @NotNull String getUsername() {
                return annotation.email();
            }

            @Override
            public Long getId() {
                return annotation.id();
            }

            @Override
            public String getEmail() {
                return annotation.email();
            }

            @Override
            public Integer getTokenVersion() {
                return 0;
            }
        };

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}