package dev.spiffocode.sigesapi.common.infrastructure.web;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringTrimmerConverter implements Converter<@NonNull String, String> {

    @Override
    public String convert(String source) {
        return source.trim();
    }
}
