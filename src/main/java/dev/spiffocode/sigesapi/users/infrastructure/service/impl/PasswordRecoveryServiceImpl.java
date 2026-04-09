package dev.spiffocode.sigesapi.users.infrastructure.service.impl;

import com.auth0.jwt.exceptions.JWTVerificationException;
import lombok.extern.slf4j.Slf4j;
import com.auth0.jwt.exceptions.TokenExpiredException;
import dev.spiffocode.sigesapi.auth.infrastructure.JwtService;
import dev.spiffocode.sigesapi.auth.infrastructure.SecurityContextHelper;
import dev.spiffocode.sigesapi.mailsender.application.service.UserManagementEmailPort;
import dev.spiffocode.sigesapi.notifications.application.service.NotificationsPort;
import dev.spiffocode.sigesapi.users.application.service.PasswordRecoveryService;
import dev.spiffocode.sigesapi.users.domain.exception.InvalidRecoveryTokenException;
import dev.spiffocode.sigesapi.users.domain.exception.RecoveryTokenExpiredException;
import dev.spiffocode.sigesapi.users.domain.model.PasswordRecoveryToken;
import dev.spiffocode.sigesapi.users.domain.model.RecoveryPlatform;
import dev.spiffocode.sigesapi.users.domain.model.User;
import dev.spiffocode.sigesapi.users.domain.repository.PasswordRecoveryTokenRepository;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import dev.spiffocode.sigesapi.users.infrastructure.properties.RecoveryProperties;
import dev.spiffocode.sigesapi.users.presentation.dto.PasswordRecoveryUpdateRequest;
import dev.spiffocode.sigesapi.users.presentation.dto.RequestAccountRecovery;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PasswordRecoveryServiceImpl implements PasswordRecoveryService {

    private final UserRepository userRepository;
    private final PasswordRecoveryTokenRepository recoveryTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RecoveryProperties recoveryProperties;
    private final UserManagementEmailPort emailPort;
    private final NotificationsPort notificationsPort;
    private final Clock clock;
    private final SecurityContextHelper securityContextHelper;

    @Async
    @Transactional
    @Override
    public void requestRecovery(RequestAccountRecovery request, String baseUrl) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            recoveryTokenRepository.deleteByUserAndUsedFalse(user);

            String jti = UUID.randomUUID().toString();
            String token = jwtService.generateRecoveryToken(jti, user.getEmail(),
                    recoveryProperties.getTokenExpiration());

            recoveryTokenRepository.save(PasswordRecoveryToken.builder()
                    .jti(jti)
                    .platform(request.platform())
                    .user(user)
                    .expiresAt(LocalDateTime.now(clock).plus(recoveryProperties.getTokenExpiration()))
                    .build());

            String recoveryUrl = UriComponentsBuilder.fromUriString(baseUrl)
                    .pathSegment("password-recovery", "redirect")
                    .queryParam("token", token)
                    .toUriString();

            emailPort.sendRecoveryEmail(
                    user.getEmail(),
                    user.fullName(),
                    token,
                    recoveryUrl);
        });
    }

    @Transactional
    @Override
    public URI redirectRecovery(String token) {
        try {
            String jti = jwtService.extractJti(token);
            PasswordRecoveryToken recoveryToken = recoveryTokenRepository.findByJti(jti)
                    .orElseThrow(() -> new InvalidRecoveryTokenException("Invalid token"));

            if (recoveryToken.isExpired(clock)) {
                return buildErrorRedirect(recoveryToken.getPlatform(), "token_expired");
            }

            if (recoveryToken.isUsed()) {
                return buildErrorRedirect(recoveryToken.getPlatform(), "token_used");
            }

            String baseUrl = recoveryToken.getPlatform() == RecoveryPlatform.MOBILE
                    ? recoveryProperties.getMobileSuccessUrl()
                    : recoveryProperties.getWebSuccessUrl();

            URI redirectUri = UriComponentsBuilder.fromUriString(baseUrl)
                    .queryParam("token", token)
                    .queryParam("email", recoveryToken.getUser().getEmail())
                    .build()
                    .toUri();

            log.info("Redirecting password recovery for platform {} to {}", recoveryToken.getPlatform(), redirectUri);
            return redirectUri;

        } catch (InvalidRecoveryTokenException | JWTVerificationException e) {
            log.warn("Failed to redirect password recovery: {}", e.getMessage());
            return buildErrorRedirect(RecoveryPlatform.WEB, "token_expired");
        }
    }

    private URI buildErrorRedirect(RecoveryPlatform platform, String reason) {
        String baseUrl;
        if (platform == RecoveryPlatform.MOBILE) {
            baseUrl = "token_used".equals(reason)
                    ? recoveryProperties.getMobileUsedUrl()
                    : recoveryProperties.getMobileExpiredUrl();
        } else {
            baseUrl = "token_used".equals(reason)
                    ? recoveryProperties.getWebUsedUrl()
                    : recoveryProperties.getWebExpiredUrl();
        }

        return UriComponentsBuilder.fromUriString(baseUrl)
                .build()
                .toUri();
    }

    @Transactional
    @Override
    public void updatePassword(PasswordRecoveryUpdateRequest request) {
        try {
            String jti = jwtService.extractJti(request.token());

            PasswordRecoveryToken recoveryToken = recoveryTokenRepository.findByJti(jti)
                    .orElseThrow(() -> new InvalidRecoveryTokenException("Invalid recovery token"));

            if (recoveryToken.isExpired(clock) || recoveryToken.isUsed()) {
                throw new RecoveryTokenExpiredException("Recovery token has already been used or has expired");
            }

            User user = recoveryToken.getUser();
            user.changePassword(passwordEncoder.encode(request.newPassword()));
            userRepository.save(user);

            recoveryToken.markAsUsed();
            recoveryTokenRepository.save(recoveryToken);

            emailPort.sendPasswordChangedEmail(user.getEmail(), user.fullName());
        } catch (TokenExpiredException e) {
            throw new RecoveryTokenExpiredException("Recovery token is expired");
        }
    }
}
