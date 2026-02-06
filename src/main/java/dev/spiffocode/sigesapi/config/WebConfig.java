package dev.spiffocode.sigesapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.accept.SemanticApiVersionParser;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .useRequestHeader("X-API-Version")
                .setDefaultVersion("1.0.0");
    }
}
