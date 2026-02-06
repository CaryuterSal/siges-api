package dev.spiffocode.sigesapi.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import dev.spiffocode.sigesapi.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final JwtProperties jwtProperties;


    @Autowired
    public JwtService(
            JwtProperties jwtProperties,
            Environment env
    ) {
        log.debug("Creating JWT Service with properties -- {}", jwtProperties);
        this.algorithm = Algorithm.HMAC256(jwtProperties.getSecret());
        this.verifier = JWT.require(algorithm).build();
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(String username, List<String> roles) {

        return JWT.create()
                .withSubject(username)
                .withClaim("roles", roles)
                .withClaim("type", "access")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getAccessExpiration()))
                .sign(algorithm);
    }

    public String generateRefreshToken(String username) {

        return JWT.create()
                .withSubject(username)
                .withClaim("type", "refresh")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getRefreshExpiration()))
                .sign(algorithm);
    }


    public DecodedJWT validate(String token) {
        return verifier.verify(token);
    }

    public String extractUsername(String token) {
        return validate(token).getSubject();
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
