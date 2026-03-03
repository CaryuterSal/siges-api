package dev.spiffocode.sigesapi.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.spiffocode.sigesapi.UnitTestClass;
import dev.spiffocode.sigesapi.auth.infrastructure.JwtService;
import dev.spiffocode.sigesapi.common.infrastructure.web.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@UnitTestClass
@Import(JwtService.class)
class JwtServiceTest {

    private static final Instant NOW = Instant.parse("2024-06-01T12:00:00Z");
    private static final ZoneId ZONE = ZoneId.of("UTC");
    private static final String SECRET = "test-secret-key-that-is-long-enough-32ch";
    private static final long ACCESS_EXPIRATION_MS  = 604_800_000L; // 7 días
    private static final long REFRESH_EXPIRATION_MS =  86_400_000L; // 1 día

    private Clock fixedClock;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(NOW, ZONE);
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setAccessExpiration(ACCESS_EXPIRATION_MS);
        props.setRefreshExpiration(REFRESH_EXPIRATION_MS);
        jwtService = new JwtService(props, fixedClock);
    }

    @Nested @DisplayName("generateAccessToken")
    class GenerateAccessToken {

        @Test @DisplayName("Válido justo después de crearse — regresión del bug reportado")
        void tokenIsValidRightAfterCreation() {
            String token = jwtService.generateAccessToken("user@example.com", List.of("ROLE_USER"), 1);
            assertThatCode(() -> jwtService.validate(token)).doesNotThrowAnyException();
        }

        @Test @DisplayName("issuedAt coincide con el clock")
        void issuedAtMatchesClock() {
            String token = jwtService.generateAccessToken("user@example.com", List.of("ROLE_USER"), 1);
            assertThat(jwtService.validate(token).getIssuedAtAsInstant()).isEqualTo(NOW);
        }

        @Test @DisplayName("expiresAt = now + accessExpiration ms")
        void expiresAtIsCorrect() {
            String token = jwtService.generateAccessToken("user@example.com", List.of("ROLE_USER"), 1);
            assertThat(jwtService.validate(token).getExpiresAtAsInstant())
                    .isEqualTo(NOW.plusMillis(ACCESS_EXPIRATION_MS));
        }

        @Test @DisplayName("Claims embebidos correctamente")
        void claimsAreCorrect() {
            String token = jwtService.generateAccessToken("user@example.com", List.of("ROLE_USER", "ROLE_ADMIN"), 3);
            DecodedJWT decoded = jwtService.validate(token);
            assertThat(decoded.getSubject()).isEqualTo("user@example.com");
            assertThat(decoded.getClaim("roles").asList(String.class)).containsExactly("ROLE_USER", "ROLE_ADMIN");
            assertThat(decoded.getClaim("token_version").asInt()).isEqualTo(3);
            assertThat(decoded.getClaim("type").asString()).isEqualTo("access");
        }

        @Test @DisplayName("isAccessToken=true, isRefreshToken=false")
        void typeClaimIsAccess() {
            String token = jwtService.generateAccessToken("user@example.com", List.of("ROLE_USER"), 1);
            assertThat(jwtService.isAccessToken(token)).isTrue();
            assertThat(jwtService.isRefreshToken(token)).isFalse();
        }

        @Test @DisplayName("Expirado 1 ms después del vencimiento → TokenExpiredException")
        void tokenIsExpiredAfterExpiryTime() {
            String token = jwtService.generateAccessToken("user@example.com", List.of("ROLE_USER"), 1);
            Clock future = Clock.fixed(NOW.plusMillis(ACCESS_EXPIRATION_MS + 1), ZONE);
            JwtProperties props = new JwtProperties();
            props.setSecret(SECRET);
            props.setAccessExpiration(ACCESS_EXPIRATION_MS);
            props.setRefreshExpiration(REFRESH_EXPIRATION_MS);
            JwtService futureService = new JwtService(props, future);
            assertThatThrownBy(() -> futureService.validate(token)).isInstanceOf(TokenExpiredException.class);
        }

        @Test @DisplayName("Cada token tiene JTI único")
        void jtiIsUnique() {
            String t1 = jwtService.generateAccessToken("user@example.com", List.of("ROLE_USER"), 1);
            String t2 = jwtService.generateAccessToken("user@example.com", List.of("ROLE_USER"), 1);
            assertThat(jwtService.extractJti(t1)).isNotEqualTo(jwtService.extractJti(t2));
        }
    }

    @Nested @DisplayName("generateRefreshToken")
    class GenerateRefreshToken {

        @Test @DisplayName("Válido justo después de crearse")
        void tokenIsValidRightAfterCreation() {
            String token = jwtService.generateRefreshToken("user@example.com", 1);
            assertThatCode(() -> jwtService.validate(token)).doesNotThrowAnyException();
        }

        @Test @DisplayName("expiresAt = now + refreshExpiration ms")
        void expiresAtIsCorrect() {
            String token = jwtService.generateRefreshToken("user@example.com", 1);
            assertThat(jwtService.validate(token).getExpiresAtAsInstant())
                    .isEqualTo(NOW.plusMillis(REFRESH_EXPIRATION_MS));
        }

        @Test @DisplayName("isRefreshToken=true, isAccessToken=false")
        void typeClaimIsRefresh() {
            String token = jwtService.generateRefreshToken("user@example.com", 1);
            assertThat(jwtService.isRefreshToken(token)).isTrue();
            assertThat(jwtService.isAccessToken(token)).isFalse();
        }

        @Test @DisplayName("token_version claim correcto")
        void tokenVersionClaim() {
            assertThat(jwtService.extractTokenVersion(
                    jwtService.generateRefreshToken("user@example.com", 7))).isEqualTo(7);
        }
    }

    @Nested @DisplayName("generateRecoveryToken")
    class GenerateRecoveryToken {

        @Test @DisplayName("Válido justo después de crearse")
        void tokenIsValidRightAfterCreation() {
            String token = jwtService.generateRecoveryToken("jti-123", "user@example.com", Duration.ofMinutes(15));
            assertThatCode(() -> jwtService.validate(token)).doesNotThrowAnyException();
        }

        @Test @DisplayName("expiresAt = now + duration")
        void expiresAtMatchesDuration() {
            Duration exp = Duration.ofMinutes(15);
            String token = jwtService.generateRecoveryToken("jti-abc", "user@example.com", exp);
            assertThat(jwtService.validate(token).getExpiresAtAsInstant()).isEqualTo(NOW.plus(exp));
        }

        @Test @DisplayName("Expirado 1s después de la duración → TokenExpiredException")
        void tokenIsExpiredAfterDuration() {
            Duration exp = Duration.ofMinutes(15);
            String token = jwtService.generateRecoveryToken("jti-xyz", "user@example.com", exp);
            Clock future = Clock.fixed(NOW.plus(exp).plusSeconds(1), ZONE);
            JwtProperties props = new JwtProperties();
            props.setSecret(SECRET);
            props.setAccessExpiration(ACCESS_EXPIRATION_MS);
            props.setRefreshExpiration(REFRESH_EXPIRATION_MS);
            assertThatThrownBy(() -> new JwtService(props, future).validate(token))
                    .isInstanceOf(TokenExpiredException.class);
        }
    }

    @Nested @DisplayName("Extractores de claims")
    class ClaimExtractors {

        @Test void extractUsername() {
            assertThat(jwtService.extractUsername(
                    jwtService.generateAccessToken("alice@example.com", List.of("ROLE_USER"), 1)))
                    .isEqualTo("alice@example.com");
        }

        @Test void extractJtiNotBlank() {
            assertThat(jwtService.extractJti(
                    jwtService.generateAccessToken("alice@example.com", List.of("ROLE_USER"), 1)))
                    .isNotBlank();
        }

        @Test void extractRoles() {
            List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
            assertThat(jwtService.extractRoles(
                    jwtService.generateAccessToken("alice@example.com", roles, 1)))
                    .containsExactlyElementsOf(roles);
        }

        @Test void extractTokenVersion() {
            assertThat(jwtService.extractTokenVersion(
                    jwtService.generateAccessToken("alice@example.com", List.of("ROLE_USER"), 42)))
                    .isEqualTo(42);
        }
    }

    @Nested @DisplayName("validate — tokens inválidos")
    class ValidateInvalidTokens {

        @Test @DisplayName("Token manipulado → JWTVerificationException")
        void tamperedToken() {
            String token = jwtService.generateAccessToken("user@example.com", List.of("ROLE_USER"), 1);
            String tampered = token.substring(0, token.length() - 4) + "XXXX";
            assertThatThrownBy(() -> jwtService.validate(tampered)).isInstanceOf(JWTVerificationException.class);
        }

        @Test @DisplayName("String aleatorio → JWTVerificationException")
        void randomStringThrows() {
            assertThatThrownBy(() -> jwtService.validate("not.a.jwt")).isInstanceOf(JWTVerificationException.class);
        }

        @Test @DisplayName("Firmado con otro secret → JWTVerificationException")
        void wrongSecretThrows() {
            JwtProperties other = new JwtProperties();
            other.setSecret("completely-different-secret-key-xyz");
            other.setAccessExpiration(ACCESS_EXPIRATION_MS);
            other.setRefreshExpiration(REFRESH_EXPIRATION_MS);
            String foreign = new JwtService(other, fixedClock)
                    .generateAccessToken("user@example.com", List.of("ROLE_USER"), 1);
            assertThatThrownBy(() -> jwtService.validate(foreign)).isInstanceOf(JWTVerificationException.class);
        }
    }
}