package dev.spiffocode.sigesapi.common.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;

@Component
public class TimezoneFilter extends OncePerRequestFilter {

    private static final String TIMEZONE_HEADER = "X-Timezone";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String timezone = request.getHeader(TIMEZONE_HEADER);

        if (timezone != null && !timezone.isBlank()) {
            try {
                TimezoneContextHolder.setZoneId(ZoneId.of(timezone));
            } catch (ZoneRulesException e) {
                // If invalid timezone, fallback to default (handled by holder)
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TimezoneContextHolder.clear();
        }
    }
}
