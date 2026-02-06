package dev.spiffocode.sigesapi.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    @Value("${security.jwt.expiration}")
    private long expiration;

    public JwtService(@Value("${security.jwt.secret}") String secret) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).build();
    }

    public String generateToken(String username, List<String> roles) {

        return JWT.create()
                .withSubject(username)
                .withClaim("roles", roles)
                .withJWTId()
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
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
}