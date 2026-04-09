package dev.spiffocode.sigesapi.common.infrastructure.web;

import com.auth0.jwt.interfaces.DecodedJWT;
import dev.spiffocode.sigesapi.auth.application.service.CustomUserDetails;
import dev.spiffocode.sigesapi.auth.infrastructure.JwtService;
import dev.spiffocode.sigesapi.auth.infrastructure.TokenBlacklistService;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);

            if (!jwtService.isAccessToken(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (tokenBlacklistService.isBlacklisted(jwtService.extractJti(token))) {
                filterChain.doFilter(request, response);
                return;
            }

            DecodedJWT jwt = jwtService.validate(token);

            String username = jwt.getSubject();

            var authorities = jwtService.extractRoles(token)
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            var userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

            if (!userDetails.isEnabled()) {
                filterChain.doFilter(request, response);
                return;
            }

            if (!Objects.equals(userDetails.getTokenVersion(), jwt.getClaim("token_version").asInt())) {
                filterChain.doFilter(request, response);
                return;
            }

            var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    authorities);

            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
