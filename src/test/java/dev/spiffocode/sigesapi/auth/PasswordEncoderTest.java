package dev.spiffocode.sigesapi.auth;

import dev.spiffocode.sigesapi.UnitTestClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@UnitTestClass
public class PasswordEncoderTest {


    private PasswordEncoder encoder;

    @BeforeEach
    void setup() {
        encoder = new DelegatingPasswordEncoder(
                "bcrypt",
                Map.of("bcrypt", new BCryptPasswordEncoder())
        );
    }

    @Test
    void encode_addsBcryptPrefix() {
        String hash = encoder.encode("secret");

        Assertions.assertNotNull(hash);
        assertTrue(hash.startsWith("{bcrypt}"));
    }

    @Test
    void encode_notPlainText() {
        String hash = encoder.encode("secret");

        assertNotEquals("secret", hash);
    }

    @Test
    void encode_samePassword_generatesDifferentHashes_dueToSalt() {
        String h1 = encoder.encode("secret");
        String h2 = encoder.encode("secret");

        assertNotEquals(h1, h2);
    }

    @Test
    void matches_correctPassword_returnsTrue() {
        String hash = encoder.encode("secret");

        assertTrue(encoder.matches("secret", hash));
    }

    @Test
    void matches_wrongPassword_returnsFalse() {
        String hash = encoder.encode("secret");

        assertFalse(encoder.matches("wrong", hash));
    }

    @Test
    void matches_tamperedHash_returnsFalse() {
        String hash = encoder.encode("secret") + "abc";

        assertFalse(encoder.matches("secret", hash));
    }

    @Test
    void matches_withoutPrefix_throwsException() {
        String rawBcrypt = new BCryptPasswordEncoder().encode("secret");

        assertThrows(IllegalArgumentException.class,
                () -> encoder.matches("secret", rawBcrypt));
    }

    @Test
    void matches_nullPassword_returnsFalse() {
        String hash = encoder.encode("secret");

        assertFalse(encoder.matches(null, hash));
    }


}
