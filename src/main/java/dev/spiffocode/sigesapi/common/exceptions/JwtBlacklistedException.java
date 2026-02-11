package dev.spiffocode.sigesapi.common.exceptions;

import com.auth0.jwt.exceptions.JWTVerificationException;

public class JwtBlacklistedException extends JWTVerificationException {
    public JwtBlacklistedException(String message) {
        super(message);
    }
}
