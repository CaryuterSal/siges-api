package dev.spiffocode.sigesapi.auth;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import dev.spiffocode.sigesapi.UnitTestClass;
import dev.spiffocode.sigesapi.auth.service.JwtService;
import dev.spiffocode.sigesapi.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@UnitTestClass
class JwtServiceTest {

    private JwtService jwt;

    @BeforeEach
    void setup() {

        JwtProperties props = new JwtProperties();
        props.setSecret("super-secret-test-key-123");
        props.setAccessExpiration(Duration.ofMinutes(5).toMillis());
        props.setRefreshExpiration(Duration.ofHours(1).toMillis());

        jwt = new JwtService(props);
    }

    @Test
    void generateAccessToken_containsCorrectClaims() {

        String token = jwt.generateAccessToken(
                "user@mail.com",
                List.of("ROLE_USER")
        );

        DecodedJWT decoded = jwt.validate(token);

        assertEquals("user@mail.com", decoded.getSubject());
        assertEquals("access", decoded.getClaim("type").asString());
        assertEquals(List.of("ROLE_USER"), decoded.getClaim("roles").asList(String.class));
        assertTrue(jwt.isAccessToken(token));
        assertFalse(jwt.isRefreshToken(token));
    }

    @Test
    void generateRefreshToken_containsCorrectClaims() {

        String token = jwt.generateRefreshToken("user@mail.com");

        DecodedJWT decoded = jwt.validate(token);

        assertEquals("user@mail.com", decoded.getSubject());
        assertEquals("refresh", decoded.getClaim("type").asString());
        assertTrue(jwt.isRefreshToken(token));
        assertFalse(jwt.isAccessToken(token));
    }


    @Test
    void extractUsername_returnsCorrectValue() {

        String token = jwt.generateAccessToken("mail@test.com", List.of());

        assertEquals("mail@test.com", jwt.extractUsername(token));
    }

    @Test
    void extractRoles_returnsCorrectValue() {

        String token = jwt.generateAccessToken("mail@test.com", List.of("A", "B"));

        assertEquals(List.of("A", "B"), jwt.extractRoles(token));
    }


    @Test
    void validate_tamperedToken_throws() {

        String token = jwt.generateAccessToken("user", List.of());

        String tampered = token + "abc";

        assertThrows(JWTVerificationException.class,
                () -> jwt.validate(tampered));
    }

    @Test
    void validate_wrongSecret_throws() {

        String token = jwt.generateAccessToken("user", List.of());

        JwtProperties otherProps = new JwtProperties();
        otherProps.setSecret("different-secret");
        otherProps.setAccessExpiration(1000L);
        otherProps.setRefreshExpiration(1000L);

        JwtService other = new JwtService(otherProps);

        assertThrows(JWTVerificationException.class,
                () -> other.validate(token));
    }


    @Test
    void expiredToken_throws() throws InterruptedException {

        JwtProperties shortProps = new JwtProperties();
        shortProps.setSecret("secret");
        shortProps.setAccessExpiration(5L);
        shortProps.setRefreshExpiration(5L);

        JwtService shortJwt = new JwtService(shortProps);

        String token = shortJwt.generateAccessToken("user", List.of());

        Thread.sleep(10);

        assertThrows(JWTVerificationException.class,
                () -> shortJwt.validate(token));
    }
}