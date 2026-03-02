package dev.spiffocode.sigesapi.users.infrastructure.validator;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    private String region;
    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    @Override
    public void initialize(PhoneNumber annotation) {
        this.region = annotation.region();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(value, region);
            return phoneUtil.isValidNumber(number);
        } catch (NumberParseException e) {
            return false;
        }
    }
}