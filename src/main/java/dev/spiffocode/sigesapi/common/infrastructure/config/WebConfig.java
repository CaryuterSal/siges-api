package dev.spiffocode.sigesapi.common.infrastructure.config;

import dev.spiffocode.sigesapi.common.infrastructure.web.StringTrimmerConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StringTrimmerConverter stringTrimmerConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringTrimmerConverter);
    }

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .useRequestHeader("X-API-Version")
                .setDefaultVersion("1.0.0");
    }
}
