package dev.spiffocode.sigesapi.users.infrastructure.service.impl;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import dev.spiffocode.sigesapi.users.application.service.PhoneNumberNormalizer;
import org.springframework.stereotype.Component;

@Component
public class GooglePhoneNumberNormalizer implements PhoneNumberNormalizer {

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    public String normalize(String raw, String region) {
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(raw, region);
            return phoneUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new IllegalArgumentException("Número de teléfono inválido: " + raw);
        }
    }
}