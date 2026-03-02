package dev.spiffocode.sigesapi.users.application.service;

public interface PhoneNumberNormalizer {
    String normalize(String raw, String region) ;
}