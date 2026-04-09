package dev.spiffocode.sigesapi.common.infrastructure.config;

import dev.spiffocode.sigesapi.common.infrastructure.web.JwtAuthenticationFilter;
import dev.spiffocode.sigesapi.common.infrastructure.web.TimezoneFilter;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
                        TimezoneFilter timezoneFilter)
                        throws Exception {
                return http
                                .anonymous(AbstractHttpConfigurer::disable)
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/auth/**").permitAll()
                                                .requestMatchers("/actuator/**").permitAll()
                                                .requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                                                .requestMatchers(
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui.html")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/buildings/**", "/spaces/**",
                                                                "/space-types/**",
                                                                "/equipments/**", "/equipment-types/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PUT, "/buildings/**", "/spaces/**",
                                                                "/space-types/**",
                                                                "/equipments/**", "/equipment-types/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.PATCH, "/buildings/**", "/spaces/**",
                                                                "/space-types/**",
                                                                "/equipments/**", "/equipment-types/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.DELETE, "/buildings/**", "/spaces/**",
                                                                "/space-types/**",
                                                                "/equipments/**", "/equipment-types/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/users/*").authenticated()
                                                .requestMatchers(HttpMethod.PATCH, "/users/*").authenticated()
                                                .requestMatchers("/users/me/**").authenticated()
                                                .requestMatchers("/admins/**", "/institutional-staff/**",
                                                                "/students/**", "/users/**")
                                                .hasRole("ADMIN")
                                                .requestMatchers("/reservations/*/approve", "/reservations/*/reject",
                                                                "/reservations/*/start", "/reservations/*/finish")
                                                .hasRole("ADMIN")
                                                .requestMatchers("/reports/**").hasRole("ADMIN")
                                                .requestMatchers(HttpMethod.GET, "/reservations/*").authenticated()
                                                .requestMatchers("/reservations/*").hasRole("APPLICANT")
                                                .requestMatchers("/password-recovery/**").permitAll()
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .addFilterBefore(timezoneFilter, JwtAuthenticationFilter.class)
                                .sessionManagement(configurer -> configurer
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                Map<String, PasswordEncoder> encoders = new HashMap<>();
                encoders.put("bcrypt", new BCryptPasswordEncoder());
                return new DelegatingPasswordEncoder("bcrypt", encoders);
        }

        @Bean
        public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
                return new ProviderManager(List.of(provider));
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = getCorsConfiguration();
                configuration.setExposedHeaders(List.of("Location", "Content-Type"));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

        @Bean
        public RoleHierarchy roleHierarchy() {
                return RoleHierarchyImpl
                                .withDefaultRolePrefix()
                                .role("INSTITUTIONAL_STAFF").implies("APPLICANT")
                                .role("STUDENT").implies("APPLICANT")
                                .build();
        }

        @Bean
        public CorsConfiguration getCorsConfiguration() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOriginPatterns(List.of("*"));
                configuration.setAllowedHeaders(List.of("*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                configuration.setAllowCredentials(true);
                return configuration;
        }

}
