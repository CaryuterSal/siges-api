package dev.spiffocode.sigesapi.auth.application.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface CustomUserDetails extends UserDetails {
    Long getId();
    String getEmail();
    Integer getTokenVersion();
}
