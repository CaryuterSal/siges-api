package dev.spiffocode.sigesapi.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access.expiration}") long accessExpiration,
            @Value("${security.jwt.refresh.expiration}") long refreshExpiration
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).build();
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateAccessToken(String username, List<String> roles) {

        return JWT.create()
                .withSubject(username)
                .withClaim("roles", roles)
                .withClaim("type", "access")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + accessExpiration))
                .sign(algorithm);
    }

    public String generateRefreshToken(String username) {

        return JWT.create()
                .withSubject(username)
                .withClaim("type", "refresh")
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshExpiration))
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
