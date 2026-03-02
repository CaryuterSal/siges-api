package dev.spiffocode.sigesapi.users.infrastructure.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneNumberValidator.class)
public @interface PhoneNumber {
    String message() default "Número de teléfono inválido";
    String region() default "MX";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}