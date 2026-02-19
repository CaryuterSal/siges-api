package dev.spiffocode.sigesapi.auth.infrastructure;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import dev.spiffocode.sigesapi.common.infrastructure.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final JwtProperties jwtProperties;
    private final Clock clock;


    @Autowired
    public JwtService(
            JwtProperties jwtProperties, Clock clock
    ) {
        this.clock = clock;
        log.debug("Creating JWT Service with properties -- {}", jwtProperties);
        this.algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        this.verifier = ((com.auth0.jwt.JWTVerifier.BaseVerification) JWT.require(algorithm)).build(clock);
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(String email, List<String> roles) {

        String jti = UUID.randomUUID().toString();

        return JWT.create()
                .withJWTId(jti)
                .withSubject(email)
                .withClaim("roles", roles)
                .withClaim("type", "access")
                .withIssuedAt(clock.instant())
                .withExpiresAt(new Date(clock.millis() + jwtProperties.getAccessExpiration()))
                .sign(algorithm);
    }

    public String generateRefreshToken(String email) {

        String jti = UUID.randomUUID().toString();

        return JWT.create()
                .withJWTId(jti)
                .withSubject(email)
                .withClaim("type", "refresh")
                .withIssuedAt(clock.instant())
                .withExpiresAt(new Date(clock.millis() + jwtProperties.getRefreshExpiration()))
                .sign(algorithm);
    }

    public DecodedJWT validate(String token) {
        return verifier.verify(token);
    }

    /**
     * Extracts JWT subject from the token
     * @param token jwt token
     * @return subject claim
     * @throws com.auth0.jwt.exceptions.JWTVerificationException when token is not valid
     */
    public String extractUsername(String token) {
        return validate(token).getSubject();
    }

    public String extractJti(String token) {
        return validate(token).getId(); // <- jti
    }

    public List<String> extractRoles(String token) {
        return validate(token).getClaim("roles").asList(String.class);
    }

    public boolean isAccessToken(String token) {
        return "access".equals(validate(token).getClaim("type").asString());
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(validate(token).getClaim("type").asString());
    }
}
